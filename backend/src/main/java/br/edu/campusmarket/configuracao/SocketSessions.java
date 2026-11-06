package br.edu.campusmarket.configuracao;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/** Encerra conexões em tempo real quando a sessão HTTP expira ou é invalidada. */
@Component
public class SocketSessions {
    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    public void add(WebSocketSession socket) {
        Object id = socket.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        if (id instanceof String sessionId) sessions.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet()).add(socket);
    }
    public void remove(WebSocketSession socket) {
        Object id = socket.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        if (id instanceof String sessionId) sessions.computeIfPresent(sessionId, (key, sockets) -> {
            sockets.remove(socket); return sockets.isEmpty() ? null : sockets;
        });
    }
    public void close(String sessionId) {
        Set<WebSocketSession> sockets = sessions.remove(sessionId);
        if (sockets == null) return;
        for (WebSocketSession socket : sockets) {
            try { if (socket.isOpen()) socket.close(CloseStatus.POLICY_VIOLATION); }
            catch (IOException ignored) { /* A conexão já pode ter sido fechada pelo cliente. */ }
        }
    }
}
