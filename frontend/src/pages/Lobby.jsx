import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getRoom, readyPlayer, startGame } from "../services/api";
import { useGame } from "../context/GameContext";
import PlayerList from "../components/PlayerList";
export default function Lobby() {
  const { session } = useGame();
  const navigate = useNavigate();
  const [room, setRoom] = useState();

  useEffect(() => {
    const load = async () => {
      const data = await getRoom(session.roomCode);
      setRoom(data);

      if (data.status === "IN_GAME") {
        navigate("/game");
      }
    };

    load();

    const i = setInterval(load, 1200);

    return () => clearInterval(i);
  }, [session.roomCode, navigate]);
  if (!room) return <main className="card">Loading...</main>;
  const me = room.players.find((p) => p.id === session.playerId),
    host = room.hostId === session.playerId;
  return (
    <main className="card">
      <h1>Lobby</h1>
      <p>
        Room code: <b>{room.roomCode}</b>
      </p>
      <PlayerList players={room.players} hostId={room.hostId} />
      <button
        onClick={() =>
          readyPlayer(room.roomCode, session.playerToken, !me.ready).then(() =>
            getRoom(room.roomCode).then(setRoom),
          )
        }
      >
        {me.ready ? "Unready" : "Ready"}
      </button>
      {host && (
        <button
          onClick={async () => {
            await startGame(room.roomCode, session.playerToken);
            navigate("/game");
          }}
        >
          Start Game
        </button>
      )}
      <button
        onClick={() =>
          navigator.clipboard.writeText(
            location.origin + "/join/" + room.roomCode,
          )
        }
      >
        Copy invite link
      </button>
    </main>
  );
}
