const API = import.meta.env.VITE_API_URL || "http://localhost:8080";

async function request(path, opt = {}) {
  const r = await fetch(API + path, {
    headers: {
      "Content-Type": "application/json",
      ...(opt.headers || {}),
    },
    ...opt,
  });

  const data = await r.json().catch(() => ({}));

  if (!r.ok) {
    throw new Error(data.message || "Request failed");
  }

  return data;
}

export const createRoom = (x) =>
  request("/api/rooms", {
    method: "POST",
    body: JSON.stringify(x),
  });

export const getRoom = (c) => request("/api/rooms/" + c);

export const joinRoom = (c, x) =>
  request("/api/rooms/" + c + "/join", {
    method: "POST",
    body: JSON.stringify(x),
  });

export const readyPlayer = (c, token, v) =>
  request(`/api/rooms/${c}/ready`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      ready: v,
    }),
  });

export const startGame = (c, token) =>
  request(`/api/rooms/${c}/start`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
