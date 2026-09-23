export default function DrawingToolbar({
  clear,
  undo,
  color,
  onColorChange,
  eraser,
  onEraserChange,
}) {
  const colors = [
    "#111111",
    "#e53935",
    "#1e88e5",
    "#43a047",
    "#fdd835",
    "#8e24aa",
    "#fb8c00",
    "#ffffff",
  ];

  return (
    <div className="toolbar">
      <span>Color:</span>

      {colors.map((c) => (
        <button
          key={c}
          type="button"
          title={c === "#ffffff" ? "White" : c}
          onClick={() => {
            onColorChange(c);
            onEraserChange(false);
          }}
          style={{
            width: 30,
            height: 30,
            padding: 0,
            margin: "0 4px",
            borderRadius: "50%",
            background: c,
            border:
              !eraser && color === c ? "3px solid #000" : "1px solid #aaa",
          }}
        />
      ))}

      <button
        type="button"
        onClick={() => onEraserChange(!eraser)}
        style={{
          marginLeft: 8,
          fontWeight: eraser ? "bold" : "normal",
          border: eraser ? "2px solid #000" : "1px solid #aaa",
        }}
      >
        🧹 Eraser
      </button>

      <button type="button" onClick={undo}>
        Undo
      </button>

      <button type="button" onClick={clear}>
        Clear
      </button>
    </div>
  );
}
