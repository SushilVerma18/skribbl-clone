import { useLocation, useNavigate } from "react-router-dom";
import { joinRoom } from "../services/api";
import { useGame } from "../context/GameContext";

export default function JoinRoom() {
  const { state } = useLocation();
  const nav = useNavigate();
  const { save } = useGame();

  return (
    <main className="card">
      <h1>Join {state.code}</h1>
      <p>{state.name}</p>

      <button
        onClick={async () => {
          const r = await joinRoom(state.code.trim().toUpperCase(), {
            playerName: state.name,
          });

          const p = r.players.at(-1);

          save({
            roomCode: r.roomCode,
            playerId: p.id,
            name: state.name,
            playerToken: r.playerToken,
          });

          nav("/lobby");
        }}
      >
        Join Room
      </button>
    </main>
  );
}
