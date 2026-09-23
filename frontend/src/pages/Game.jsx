import { useEffect, useState } from "react";
import { useGame } from "../context/GameContext";
import { openRoom } from "../services/websocket";
import Canvas from "../components/Canvas";
import Chat from "../components/Chat";
import WordSelection from "../components/WordSelection";
import DrawingToolbar from "../components/DrawingToolbar";

export default function Game() {
  const { session } = useGame();

  const [client, setClient] = useState(),
    [phase, setPhase] = useState("WORD_SELECTION"),
    [color, setColor] = useState("#111111"),
    [eraser, setEraser] = useState(false),
    [leaderboard, setLeaderboard] = useState([]),
    [drawer, setDrawer] = useState(""),
    [options, setOptions] = useState([]),
    [word, setWord] = useState(""),
    [hint, setHint] = useState(""),
    [messages, setMessages] = useState([]),
    [events, setEvents] = useState([]),
    [scores, setScores] = useState([]),
    [timer, setTimer] = useState(0);

  useEffect(() => {
    const c = openRoom(session.roomCode, session.playerToken, {
      game: (m) => {
        setPhase(m.phase);
        setDrawer(String(m.drawerId));

        if (m.phase === "GAME_OVER") {
          setLeaderboard(m.leaderboard || []);
        }

        if (m.phase === "WORD_SELECTION") {
          setHint("");
        }

        if (m.phase === "DRAWING" && m.hint) {
          setHint(m.hint);
        }

        if (m.phase === "ROUND_END") {
          setHint("");
          setMessages((x) => [
            ...x,
            {
              playerName: "System",
              text: `Answer: ${m.word}`,
            },
          ]);
        }
      },

      "word-options": (m) => {
        setOptions(m.wordOptions || []);
        setDrawer(String(m.drawerId));
      },

      "word-private": (m) => {
        if (String(m.drawerId) === String(session.playerId)) {
          setWord(m.word);
        }
      },

      hint: (m) => {
        setHint(m.hint || "");
      },

      drawing: (m) => {
        setEvents((x) => [...x, m]);
      },

      chat: (m) => {
        setMessages((x) => [...x, m]);
      },

      scores: (m) => {
        setScores(m);
      },

      timer: (m) => {
        setTimer(m.remainingTime);
      },
    });

    setClient(c);

    return () => c.deactivate();
  }, [session.roomCode, session.playerId, session.playerToken]);

  const publish = (dest, payload) =>
    client?.publish({
      destination: dest,
      body: JSON.stringify(payload),
    });

  const isDrawer = String(drawer) === String(session.playerId);

  return (
    <main className="game">
      <header>
        <h1>🎨 Skribbl Clone</h1>

        <span>Round phase: {phase}</span>

        <strong>Time: {timer}s</strong>

        {isDrawer && word && phase === "DRAWING" && <b>Your word: {word}</b>}

        {!isDrawer && phase === "DRAWING" && hint && (
          <div className="hint">
            <strong>Hint:</strong> {hint}
          </div>
        )}
      </header>

      {isDrawer && phase === "WORD_SELECTION" && (
        <WordSelection
          options={options}
          onChoose={(w) =>
            publish(`/app/room/${session.roomCode}/choose-word`, {
              playerId: session.playerId,
              word: w,
            })
          }
        />
      )}

      {phase === "GAME_OVER" && (
        <div className="panel">
          <h2>Game Over</h2>

          {leaderboard.map((s, i) => (
            <div className="row" key={s.playerId}>
              <span>
                {i + 1}. {s.playerName}
              </span>

              <b>{s.score}</b>
            </div>
          ))}
        </div>
      )}

      <section className="layout">
        <div>
          <Canvas
            client={client}
            room={session.roomCode}
            playerId={session.playerId}
            enabled={isDrawer && phase === "DRAWING"}
            color={color}
            eraser={eraser}
            events={events}
          />

          {isDrawer && phase === "DRAWING" && (
            <DrawingToolbar
              color={color}
              onColorChange={setColor}
              eraser={eraser}
              onEraserChange={setEraser}
              clear={() =>
                publish(`/app/room/${session.roomCode}/canvas`, {
                  playerId: session.playerId,
                  type: "CANVAS_CLEAR",
                })
              }
              undo={() =>
                publish(`/app/room/${session.roomCode}/canvas`, {
                  playerId: session.playerId,
                  type: "CANVAS_UNDO",
                })
              }
            />
          )}

          <div className="panel">
            <b>Scores</b>

            {scores.map((s) => (
              <div className="row" key={s.playerId}>
                <span>{s.playerName}</span>

                <span>{s.score}</span>
              </div>
            ))}
          </div>
        </div>

        <aside>
          <Chat
            messages={messages}
            send={(text) =>
              publish(`/app/room/${session.roomCode}/guess`, {
                playerId: session.playerId,
                text,
              })
            }
          />
        </aside>
      </section>
    </main>
  );
}
