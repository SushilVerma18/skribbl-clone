import { useEffect, useRef } from "react";
export default function Canvas({
  client,
  room,
  playerId,
  enabled,
  color = "#111111",
  eraser = false,
  events = [],
}) {
  const ref = useRef(),
    points = useRef([]),
    down = useRef(false);
  const ctx = () => ref.current.getContext("2d");
  const pos = (e) => {
    const r = ref.current.getBoundingClientRect();
    return {
      x: ((e.clientX - r.left) * 900) / r.width,
      y: ((e.clientY - r.top) * 560) / r.height,
    };
  };
  useEffect(() => {
    for (const d of events) {
      const c = ctx();
      if (d.type === "CANVAS_CLEAR") {
        c.clearRect(0, 0, 900, 560);
        continue;
      }
      if (d.type === "stroke" && Array.isArray(d.points)) {
        c.globalCompositeOperation = d.eraser
          ? "destination-out"
          : "source-over";

        c.strokeStyle = d.eraser ? "rgba(0,0,0,1)" : d.color || "#111111";

        c.lineWidth = Number(d.size) || 4;
        c.lineCap = "round";
        c.beginPath();
        d.points.forEach((p, i) =>
          i ? c.lineTo(p.x, p.y) : c.moveTo(p.x, p.y),
        );
        c.stroke();

        c.globalCompositeOperation = "source-over";
      }
      if (d.type === "CANVAS_UNDO") c.clearRect(0, 0, 900, 560);
    }
  }, [events]);
  const start = (e) => {
    if (!enabled) return;
    down.current = true;
    points.current = [pos(e)];
  };
  const move = (e) => {
    if (!down.current) return;
    const p = pos(e),
      q = points.current.at(-1);
    points.current.push(p);
    const c = ctx();
    c.globalCompositeOperation = eraser ? "destination-out" : "source-over";
    c.strokeStyle = eraser ? "rgba(0,0,0,1)" : color;
    c.lineWidth = eraser ? 25 : 4;
    c.lineCap = "round";
    c.beginPath();
    c.moveTo(q.x, q.y);
    c.lineTo(p.x, p.y);
    c.stroke();
  };
  const end = () => {
    if (!down.current) return;
    down.current = false;
    client?.publish({
      destination: `/app/room/${room}/draw`,
      body: JSON.stringify({
        playerId,
        type: "stroke",
        color,
        eraser,
        size: eraser ? 25 : 4,
        points: points.current,
      }),
    });
    points.current = [];
  };
  return (
    <canvas
      ref={ref}
      width="900"
      height="560"
      className="canvas"
      onMouseDown={start}
      onMouseMove={move}
      onMouseUp={end}
      onMouseLeave={end}
    />
  );
}
