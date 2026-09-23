import { Client } from "@stomp/stompjs";

const URL =
  import.meta.env.VITE_WS_URL ||
  (location.protocol === "https:"
    ? `wss://${location.host}/ws`
    : "ws://localhost:8080/ws");

export function openRoom(room, playerToken, handlers) {
  const client = new Client({
    brokerURL: URL,

    connectHeaders: {
      Authorization: `Bearer ${playerToken}`,
    },

    reconnectDelay: 3000,

    onConnect: () => {
      Object.entries(handlers).forEach(([name, fn]) => {
        const destination =
          name === "word-private"
            ? "/user/queue/word-private"
            : name === "word-options"
              ? "/user/queue/word-options"
              : `/topic/room/${room}/${name}`;

        client.subscribe(destination, (message) => {
          fn(JSON.parse(message.body));
        });
      });

      client.publish({
        destination: `/app/room/${room}/state`,
        body: "{}",
      });
    },
  });

  client.activate();

  return client;
}
