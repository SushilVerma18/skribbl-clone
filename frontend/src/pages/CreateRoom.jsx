import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { createRoom } from "../services/api";
import { useGame } from "../context/GameContext";
export default function CreateRoom() {
  const { state } = useLocation(),
    nav = useNavigate(),
    { save } = useGame();
  const [f, setF] = useState({
    playerName: state?.name || "",
    isPrivate: false,
    maxPlayers: 8,
    rounds: 3,
    drawTime: 60,
    wordCount: 3,
    hints: 2,
    wordMode: "NORMAL",
  });
  const set = (k, v) => setF((x) => ({ ...x, [k]: v }));
  return (
    <main className="card">
      <h1>Create Room</h1>
      <input
        value={f.playerName}
        onChange={(e) => set("playerName", e.target.value)}
        placeholder="Player name"
      />
      {[
        ["maxPlayers", "Max players"],
        ["rounds", "Rounds"],
        ["drawTime", "Draw seconds"],
        ["wordCount", "Word choices"],
        ["hints", "Hints"],
      ].map(([k, l]) => (
        <label key={k}>
          {l}
          <input
            type="number"
            value={f[k]}
            onChange={(e) => set(k, Number(e.target.value))}
          />
        </label>
      ))}
      <label>
        Private{" "}
        <input
          type="checkbox"
          checked={f.isPrivate}
          onChange={(e) => set("isPrivate", e.target.checked)}
        />
      </label>
      <label>
        Word mode
        <select
          value={f.wordMode}
          onChange={(e) => set("wordMode", e.target.value)}
        >
          <option>NORMAL</option>
          <option>HIDDEN</option>
          <option>COMBINATION</option>
        </select>
      </label>
      <button
        onClick={async () => {
          const r = await createRoom(f);

          save({
            roomCode: r.roomCode,
            playerId: r.hostId,
            name: f.playerName,
            playerToken: r.playerToken,
          });

          nav("/lobby");
        }}
      >
        Create
      </button>
    </main>
  );
}
