package com.example.skribbl.websocket;

import com.example.skribbl.security.PlayerSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    String origins;

    private final PlayerSessionService sessions;

    public WebSocketConfig(PlayerSessionService sessions) {
        this.sessions = sessions;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableSimpleBroker(
                "/topic",
                "/queue"
        );

        registry.setApplicationDestinationPrefixes("/app");

        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        origins.split(",")
                );
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {

        registration.interceptors(
                new ChannelInterceptor() {

                    @Override
                    public Message<?> preSend(
                            Message<?> message,
                            MessageChannel channel
                    ) {

                        StompHeaderAccessor accessor =
                                MessageHeaderAccessor.getAccessor(
                                        message,
                                        StompHeaderAccessor.class
                                );

                        if (accessor == null) {
                            return message;
                        }

                        if (
                                StompCommand.CONNECT.equals(
                                        accessor.getCommand()
                                )
                        ) {

                            String authorization =
                                    accessor.getFirstNativeHeader(
                                            "Authorization"
                                    );

                            if (
                                    authorization == null
                                            || !authorization.startsWith(
                                            "Bearer "
                                    )
                            ) {

                                throw new SecurityException(
                                        "Missing WebSocket authorization token"
                                );
                            }

                            String token =
                                    authorization.substring(7);

                            UUID playerId =
                                    sessions.authenticate(token);

                            Principal principal =
                                    () -> playerId.toString();

                            accessor.setUser(principal);
                        }

                        return message;
                    }
                }
        );
    }
}