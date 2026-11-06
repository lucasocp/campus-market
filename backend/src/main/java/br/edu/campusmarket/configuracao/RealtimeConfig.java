package br.edu.campusmarket.configuracao;

import br.edu.campusmarket.common.CatalogEvents;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import jakarta.servlet.http.*;

@Configuration @EnableWebSocketMessageBroker
public class RealtimeConfig implements WebSocketMessageBrokerConfigurer {
    private final SocketSessions sockets;
    public RealtimeConfig(SocketSessions sockets) { this.sockets = sockets; }
    public record CatalogUpdate(UUID listingId) { }
    public record PrivateUpdate(String ra) { }
    @Override public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Sem origens curinga: o handshake aceita somente a mesma origem.
        var session = new HttpSessionHandshakeInterceptor();
        session.setCopyAllAttributes(false);
        session.setCopyHttpSessionId(true);
        registry.addEndpoint("/ws").addInterceptors(session);
    }
    @Override public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                sockets.add(session); super.afterConnectionEstablished(session);
            }
            @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                sockets.remove(session); super.afterConnectionClosed(session, status);
            }
        });
    }
    @Bean ServletListenerRegistrationBean<HttpSessionListener> sessionListener(SocketSessions sockets) {
        return new ServletListenerRegistrationBean<>(new HttpSessionListener() {
            @Override public void sessionDestroyed(HttpSessionEvent event) { sockets.close(event.getSession().getId()); }
        });
    }
    @Override public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
    }
    @Override public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override public Message<?> preSend(Message<?> message, MessageChannel channel) {
                var header = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (header == null || header.getCommand() == null) return message;
                if (header.getUser() == null) throw new MessagingException("Sessão necessária.");
                if (header.getCommand() == StompCommand.SEND) throw new MessagingException("Envie mensagens pela API autenticada.");
                if (header.getCommand() == StompCommand.SUBSCRIBE
                    && !Set.of("/topic/catalog", "/user/queue/updates").contains(header.getDestination()))
                    throw new MessagingException("Canal não autorizado.");
                return message;
            }
        });
    }
    @Bean CatalogEvents catalogEvents(ApplicationEventPublisher events) {
        return id -> events.publishEvent(new CatalogUpdate(id));
    }
    @Component public static class BroadcastAfterCommit {
        private final SimpMessagingTemplate messaging;
        public BroadcastAfterCommit(SimpMessagingTemplate messaging) { this.messaging = messaging; }
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void catalog(CatalogUpdate event) { messaging.convertAndSend("/topic/catalog", (Object) Map.of("type", "catalog", "listingId", event.listingId())); }
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void privateUpdate(PrivateUpdate event) { messaging.convertAndSendToUser(event.ra(), "/queue/updates", Map.of("type", "update")); }
    }
}
