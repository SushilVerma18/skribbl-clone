package com.example.skribbl.service;

import com.example.skribbl.entity.Room;
import com.example.skribbl.enums.*;
import com.example.skribbl.exception.InvalidGameStateException;
import com.example.skribbl.model.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class GameService {

    private final RoomService rooms;
    private final WordService words;
    private final ScoringService scoring;
    private final SimpMessagingTemplate broker;

    public GameService(
            RoomService rooms,
            WordService words,
            ScoringService scoring,
            SimpMessagingTemplate broker
    ) {
        this.rooms = rooms;
        this.words = words;
        this.scoring = scoring;
        this.broker = broker;
    }

    private String topic(String code, String name) {
        return "/topic/room/" + code + "/" + name;
    }

    public synchronized void start(String code, UUID host) {

        rooms.requireHost(code, host);

        Room r = rooms.find(code);
        RoomState s = rooms.state(r);

        if (s.game.phase != GamePhase.LOBBY)
            throw new InvalidGameStateException("Game already started");

        if (s.players.size() < 2)
            throw new InvalidGameStateException(
                    "At least two players are required"
            );

        s.game.turnOrder.clear();
        s.game.turnOrder.addAll(s.players.keySet());

        s.game.turnIndex = 0;
        s.game.round = 0;

        rooms.markInGame(code);

        beginRound(code, s);
    }

    private void beginRound(String code, RoomState s) {

        if (s.game.round >= s.game.totalRounds) {
            finish(code, s);
            return;
        }

        s.game.round++;

        s.game.drawerId =
                s.game.turnOrder.get(
                        s.game.turnIndex % s.game.turnOrder.size()
                );

        s.game.phase = GamePhase.WORD_SELECTION;
        s.game.word = null;
        s.game.roundEnd = null;
        s.game.hintStart = null;
        s.game.guessed.clear();

        s.game.wordOptions =
                words.choose(
                        rooms.find(code)
                                .getSettings()
                                .getWordCount()
                );

        broker.convertAndSend(
                topic(code, "game"),
                Map.of(
                        "phase", s.game.phase,
                        "round", s.game.round,
                        "totalRounds", s.game.totalRounds,
                        "drawerId", s.game.drawerId,
                        "wordLength", 0,
                        "hint", ""
                )
        );

        broker.convertAndSendToUser(
                s.game.drawerId.toString(),
                "/queue/word-options",
                Map.of(
                        "drawerId", s.game.drawerId,
                        "wordOptions", s.game.wordOptions
                )
        );
    }

    public synchronized void chooseWord(
            String code,
            UUID playerId,
            String word
    ) {

        RoomState s =
                rooms.state(
                        rooms.find(code)
                );

        if (
                !playerId.equals(s.game.drawerId)
                        || s.game.phase != GamePhase.WORD_SELECTION
                        || !s.game.wordOptions.contains(
                        words.normalize(word)
                )
        ) {
            throw new InvalidGameStateException(
                    "Invalid word selection"
            );
        }

        s.game.word = words.normalize(word);
        s.game.phase = GamePhase.DRAWING;

        Instant now = Instant.now();

        int drawTime =
                rooms.find(code)
                        .getSettings()
                        .getDrawTime();

        s.game.roundEnd =
                now.plusSeconds(drawTime);

        s.game.hintStart =
                now.plusSeconds(
                        Math.max(
                                5,
                                drawTime / 3
                        )
                );

        broker.convertAndSend(
                topic(code, "game"),
                Map.of(
                        "phase", "DRAWING",
                        "round", s.game.round,
                        "totalRounds", s.game.totalRounds,
                        "drawerId", s.game.drawerId,
                        "wordLength", s.game.word.length(),
                        "hint", "_".repeat(
                                s.game.word.length()
                        )
                )
        );

        broker.convertAndSendToUser(
                playerId.toString(),
                "/queue/word-private",
                Map.of(
                        "drawerId", playerId,
                        "word", s.game.word
                )
        );
    }

    public synchronized void draw(
            String code,
            UUID playerId,
            Map<String, Object> payload
    ) {

        RoomState s =
                rooms.state(
                        rooms.find(code)
                );

        if (
                !playerId.equals(s.game.drawerId)
                        || s.game.phase != GamePhase.DRAWING
        ) {
            throw new InvalidGameStateException(
                    "Only the current drawer can draw"
            );
        }

        broker.convertAndSend(
                topic(code, "drawing"),
                payload
        );
    }

    public synchronized void canvasCommand(
            String code,
            UUID playerId,
            String command
    ) {

        RoomState s =
                rooms.state(
                        rooms.find(code)
                );

        if (
                !playerId.equals(s.game.drawerId)
                        || s.game.phase != GamePhase.DRAWING
        ) {
            throw new InvalidGameStateException(
                    "Only the current drawer can modify the canvas"
            );
        }

        broker.convertAndSend(
                topic(code, "drawing"),
                Map.of(
                        "type",
                        command
                )
        );
    }

    public synchronized Map<String, Object> guess(
            String code,
            UUID playerId,
            String text
    ) {

        RoomState s =
                rooms.state(
                        rooms.find(code)
                );

        if (
                s.game.phase != GamePhase.DRAWING
                        || playerId.equals(s.game.drawerId)
        ) {
            return Map.of(
                    "correct",
                    false
            );
        }

        if (s.game.guessed.contains(playerId)) {
            return Map.of(
                    "correct",
                    false,
                    "duplicate",
                    true
            );
        }

        boolean correct =
                words.matches(
                        text,
                        s.game.word
                );

        PlayerState p =
                s.players.get(playerId);

        if (p == null) {
            return Map.of(
                    "correct",
                    false
            );
        }

        if (correct) {

            s.game.guessed.add(playerId);

            int rank =
                    s.game.guessed.size();

            long remaining =
                    Math.max(
                            0,
                            Duration.between(
                                    Instant.now(),
                                    s.game.roundEnd
                            ).getSeconds()
                    );

            p.score += scoring.points(
                    remaining,
                    rooms.find(code)
                            .getSettings()
                            .getDrawTime(),
                    rank
            );

            broker.convertAndSend(
                    topic(code, "scores"),
                    leaderboard(s)
            );

            broker.convertAndSend(
                    topic(code, "chat"),
                    Map.of(
                            "type",
                            "CORRECT_GUESS",
                            "playerName",
                            p.name,
                            "points",
                            p.score
                    )
            );

            if (
                    s.game.guessed.size()
                            >= s.players.size() - 1
            ) {
                endRound(code, s);
            }

        } else {

            broker.convertAndSend(
                    topic(code, "chat"),
                    Map.of(
                            "type",
                            "CHAT",
                            "playerId",
                            playerId,
                            "playerName",
                            p.name,
                            "text",
                            text
                    )
            );
        }

        return Map.of(
                "correct",
                correct
        );
    }

    private void endRound(
            String code,
            RoomState s
    ) {

        if (s.game.phase != GamePhase.DRAWING)
            return;

        s.game.phase = GamePhase.ROUND_END;

        broker.convertAndSend(
                topic(code, "game"),
                Map.of(
                        "phase",
                        "ROUND_END",
                        "round",
                        s.game.round,
                        "word",
                        s.game.word,
                        "scores",
                        leaderboard(s)
                )
        );

        s.game.turnIndex++;

        beginRound(code, s);
    }

    private void finish(
            String code,
            RoomState s
    ) {

        s.game.phase = GamePhase.GAME_OVER;

        List<Map<String, Object>> board =
                leaderboard(s);

        broker.convertAndSend(
                topic(code, "game"),
                Map.of(
                        "phase",
                        "GAME_OVER",
                        "leaderboard",
                        board,
                        "winner",
                        board.isEmpty()
                                ? null
                                : board.get(0)
                )
        );
    }

    private List<Map<String, Object>> leaderboard(
            RoomState s
    ) {

        return s.players.values()
                .stream()
                .sorted(
                        (a, b) ->
                                Integer.compare(
                                        b.score,
                                        a.score
                                )
                )
                .map(
                        p ->
                                Map.<String, Object>of(
                                        "playerId",
                                        p.id,
                                        "playerName",
                                        p.name,
                                        "score",
                                        p.score
                                )
                )
                .toList();
    }

    private String buildHint(
            String word,
            GameState game,
            int maxHints
    ) {

        if (word == null || word.isEmpty()) {
            return "";
        }

        // Hints disabled
        if (maxHints <= 0) {
            return maskedWord(word);
        }

        if (game.hintStart == null || game.roundEnd == null) {
            return maskedWord(word);
        }

        long totalHintTime =
                Duration.between(
                        game.hintStart,
                        game.roundEnd
                ).getSeconds();

        long elapsed =
                Duration.between(
                        game.hintStart,
                        Instant.now()
                ).getSeconds();

        // Hint timer has not started yet
        if (elapsed < 0) {
            return maskedWord(word);
        }

        // Find positions of actual letters
        List<Integer> letterPositions = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) != ' ') {
                letterPositions.add(i);
            }
        }

        if (letterPositions.isEmpty()) {
            return word;
        }

        /*
         * Never reveal the complete word through hints.
         * For example:
         * 8-letter word + 8 hints -> reveal at most 7 letters.
         */
        int maximumReveal =
                Math.min(
                        maxHints,
                        Math.max(0, letterPositions.size() - 1)
                );

        if (maximumReveal == 0) {
            return maskedWord(word);
        }

        /*
         * Reveal the configured number of letters gradually.
         */
        int revealed;

        if (totalHintTime <= 0) {
            revealed = maximumReveal;
        } else {

            long interval =
                    Math.max(
                            1,
                            totalHintTime / maximumReveal
                    );

            revealed =
                    (int) Math.min(
                            maximumReveal,
                            (elapsed / interval) + 1
                    );
        }

        Set<Integer> revealedPositions =
                new HashSet<>();

        /*
         * Spread revealed letters across the word
         * instead of always revealing from left to right.
         */
        if (revealed == 1) {

            int middle =
                    letterPositions.size() / 2;

            revealedPositions.add(
                    letterPositions.get(middle)
            );

        } else {

            for (int i = 0; i < revealed; i++) {

                int index =
                        Math.round(
                                (float) i
                                        * (letterPositions.size() - 1)
                                        / (revealed - 1)
                        );

                revealedPositions.add(
                        letterPositions.get(index)
                );
            }
        }

        StringBuilder hint =
                new StringBuilder();

        for (int i = 0; i < word.length(); i++) {

            char ch = word.charAt(i);

            if (ch == ' ') {
                hint.append(' ');
            } else if (revealedPositions.contains(i)) {
                hint.append(ch);
            } else {
                hint.append('_');
            }

            if (i < word.length() - 1) {
                hint.append(' ');
            }
        }

        return hint.toString();
    }

    private String maskedWord(String word) {

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {

            if (word.charAt(i) == ' ') {
                result.append(' ');
            } else {
                result.append('_');
            }

            if (i < word.length() - 1) {
                result.append(' ');
            }
        }

        return result.toString();
    }

    public synchronized void sendCurrentState(
            String code
    ) {

        RoomState s =
                rooms.state(
                        rooms.find(code)
                );

        Map<String, Object> state =
                new HashMap<>();

        state.put(
                "phase",
                s.game.phase
        );

        state.put(
                "round",
                s.game.round
        );

        state.put(
                "totalRounds",
                s.game.totalRounds
        );

        state.put(
                "drawerId",
                s.game.drawerId
        );

        state.put(
                "wordLength",
                s.game.word == null
                        ? 0
                        : s.game.word.length()
        );

        state.put(
                "hint",
                s.game.word == null
                        ? ""
                        : buildHint(
                        s.game.word,
                        s.game,
                        rooms.find(code).getSettings().getHints()
                )
        );

        broker.convertAndSend(
                topic(code, "game"),
                state
        );

        if (
                s.game.phase == GamePhase.WORD_SELECTION
                        && s.game.drawerId != null
        ) {

            broker.convertAndSendToUser(
                    s.game.drawerId.toString(),
                    "/queue/word-options",
                    Map.of(
                            "drawerId",
                            s.game.drawerId,
                            "wordOptions",
                            s.game.wordOptions
                    )
            );
        }

        if (
                s.game.phase == GamePhase.DRAWING
                        && s.game.word != null
                        && s.game.drawerId != null
        ) {

            broker.convertAndSendToUser(
                    s.game.drawerId.toString(),
                    "/queue/word-private",
                    Map.of(
                            "drawerId",
                            s.game.drawerId,
                            "word",
                            s.game.word
                    )
            );
        }
    }

    @Scheduled(fixedRate = 500)
    public void timer() {

        for (var entry : rooms.activeStates()) {

            RoomState s = entry.getValue();

            if (
                    s.game.phase == GamePhase.DRAWING
                            && s.game.roundEnd != null
            ) {

                String code =
                        rooms.codeFor(
                                entry.getKey()
                        );

                if (code == null)
                    continue;

                long remaining =
                        Math.max(
                                0,
                                Duration.between(
                                        Instant.now(),
                                        s.game.roundEnd
                                ).getSeconds()
                        );

                broker.convertAndSend(
                        topic(code, "timer"),
                        Map.of(
                                "remainingTime",
                                remaining,
                                "round",
                                s.game.round
                        )
                );

                if (
                        s.game.word != null
                                && s.game.hintStart != null
                ) {

                    String hint =
                            buildHint(
                                    s.game.word,
                                    s.game,
                                    rooms.find(code)
                                            .getSettings()
                                            .getHints()
                            );

                    broker.convertAndSend(
                            topic(code, "hint"),
                            Map.of(
                                    "hint",
                                    hint,
                                    "round",
                                    s.game.round
                            )
                    );
                }

                if (remaining <= 0) {
                    synchronized (this) {
                        endRound(code, s);
                    }
                }
            }
        }
    }
}