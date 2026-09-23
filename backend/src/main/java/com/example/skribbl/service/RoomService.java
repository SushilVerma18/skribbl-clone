package com.example.skribbl.service;
import com.example.skribbl.dto.request.*;
import com.example.skribbl.dto.response.RoomResponse;
import com.example.skribbl.entity.*;
import com.example.skribbl.enums.*;
import com.example.skribbl.exception.*;
import com.example.skribbl.model.*;
import com.example.skribbl.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.example.skribbl.security.PlayerSession;
import com.example.skribbl.security.PlayerSessionService;

@Service
public class RoomService {
    private final RoomRepository rooms; private final PlayerRepository players;
    private final Map<UUID,RoomState> states=new ConcurrentHashMap<>();
    private final Random random=new Random();
    private final PlayerSessionService sessions;
    public RoomService(
            RoomRepository rooms,
            PlayerRepository players,
            PlayerSessionService sessions
    ) {
        this.rooms = rooms;
        this.players = players;
        this.sessions = sessions;
    }

    public synchronized RoomResponse create(CreateRoomRequest q){
        RoomSettings s=new RoomSettings();s.setMaxPlayers(q.maxPlayers());s.setRounds(q.rounds());s.setDrawTime(q.drawTime());
        s.setWordCount(q.wordCount());s.setHints(q.hints());s.setWordMode(q.wordMode());
        Room r=new Room();r.setCode(generateCode());r.setVisibility(q.isPrivate()?RoomVisibility.PRIVATE:RoomVisibility.PUBLIC);r.setSettings(s);
        UUID host = UUID.randomUUID();

        r.setHostPlayerId(host);
        rooms.save(r);

        players.save(
                new Player(
                        host,
                        q.playerName(),
                        r.getId()
                )
        );

        PlayerSession session =
                sessions.create(host);
        RoomState st=new RoomState(r.getId(),s.getRounds());st.players.put(host,new PlayerState(host,q.playerName()));states.put(r.getId(),st);
        return response(r, st, session.token());
    }

    public synchronized RoomResponse join(String code,JoinRoomRequest q){
        Room r=find(code);RoomState st=state(r);
        if(r.getStatus()!=RoomStatus.LOBBY)throw new InvalidGameStateException("Game has already started");
        if(st.players.size()>=r.getSettings().getMaxPlayers())throw new RoomFullException("Room is full");
        UUID id = UUID.randomUUID();

        players.save(
                new Player(
                        id,
                        q.playerName(),
                        r.getId()
                )
        );

        st.players.put(
                id,
                new PlayerState(id, q.playerName())
        );

        PlayerSession session =
                sessions.create(id);

        return response(r, st, session.token());
    }

    public Room find(String code){return rooms.findByCodeIgnoreCase(code).orElseThrow(()->new RoomNotFoundException("Room not found"));}

    public RoomState state(Room r){
        return states.computeIfAbsent(r.getId(),id->{
            RoomState s=new RoomState(id,r.getSettings().getRounds());
            for(Player p:players.findByRoomId(id)){PlayerState ps=new PlayerState(p.getId(),p.getName());ps.score=p.getScore();ps.ready=p.isReady();ps.connected=p.isConnected();s.players.put(ps.id,ps);}
            return s;
        });
    }

    public RoomResponse get(String code) {
        Room r = find(code);
        return response(r, state(r), null);
    }
    public void requireHost(String code,UUID playerId){
        if(!find(code).getHostPlayerId().equals(playerId))throw new NotRoomHostException("Only the host can do this");
    }

    public void requirePlayer(String code, UUID playerId) {

        if (playerId == null) {
            throw new InvalidGameStateException(
                    "Player ID is required"
            );
        }

        Room r = find(code);
        RoomState s = state(r);

        if (!s.players.containsKey(playerId)) {
            throw new InvalidGameStateException(
                    "Player is not a member of this room"
            );
        }
    }
    public synchronized void setReady(String code,UUID id,boolean ready){
        Room r=find(code);RoomState s=state(r);PlayerState p=s.players.get(id);if(p==null)throw new InvalidGameStateException("Player not found");
        p.ready=ready;Player db=players.findById(id).orElseThrow();db.setReady(ready);players.save(db);
    }
    public List<Room> publicRooms(){return rooms.findByVisibilityAndStatus(RoomVisibility.PUBLIC,RoomStatus.LOBBY);}
    public Collection<Map.Entry<UUID,RoomState>> activeStates(){return List.copyOf(states.entrySet());}
    public String codeFor(UUID id){return rooms.findById(id).map(Room::getCode).orElse(null);}
    private String generateCode(){String chars="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";while(true){StringBuilder b=new StringBuilder();for(int i=0;i<6;i++)b.append(chars.charAt(random.nextInt(chars.length())));if(rooms.findByCodeIgnoreCase(b.toString()).isEmpty())return b.toString();}}

    private RoomResponse response(
            Room r,
            RoomState s,
            String playerToken
    ){
        List<Map<String,Object>> ps=s.players.values().stream().map(p->Map.<String,Object>of(
          "id",p.id,"name",p.name,"score",p.score,"ready",p.ready,"connected",p.connected)).toList();
        return new RoomResponse(
                r.getId(),
                r.getCode(),
                r.getVisibility(),
                r.getStatus(),
                r.getHostPlayerId(),
                playerToken,
                r.getSettings(),
                ps
        );
    }

    public synchronized void markInGame(String code){
        Room r=find(code);
        r.setStatus(RoomStatus.IN_GAME);
        rooms.save(r);
    }
}
