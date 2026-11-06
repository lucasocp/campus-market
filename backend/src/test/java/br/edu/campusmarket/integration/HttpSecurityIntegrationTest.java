package br.edu.campusmarket.integration;

import br.edu.campusmarket.usuarios.domain.AppUser;
import br.edu.campusmarket.usuarios.infrastructure.UserJpaRepository;
import java.net.*;
import java.net.http.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HttpSecurityIntegrationTest {
    @LocalServerPort int port;
    @Autowired UserJpaRepository users;
    @Autowired PasswordEncoder passwords;
    private static final AtomicInteger sequence = new AtomicInteger(80000000);
    private HttpClient client;
    private String csrf;
    @BeforeEach void prepare() throws Exception {
        client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        var response = request("GET", "/auth/csrf", null, false);
        var matcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(response.body());
        assertTrue(matcher.find()); csrf = matcher.group(1);
    }
    private HttpResponse<String> request(String method, String path, String body, boolean withCsrf) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api" + path));
        if (withCsrf) builder.header("X-CSRF-TOKEN", csrf);
        builder.header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    private void login() throws Exception {
        String ra = String.valueOf(sequence.incrementAndGet());
        users.save(new AppUser(ra, "Estudante HTTP", "Computação", passwords.encode("Senha@123")));
        assertEquals(200, request("POST", "/auth/login", "{\"ra\":\"" + ra + "\",\"password\":\"Senha@123\"}", true).statusCode());
    }
    @Test void anonymousCannotReadCatalog() throws Exception { assertEquals(401, request("GET", "/listings", null, false).statusCode()); }
    @Test void loginWithoutCsrfIsRejected() throws Exception {
        assertEquals(403, request("POST", "/auth/login", "{\"ra\":\"20260001\",\"password\":\"Senha@123\"}", false).statusCode());
    }
    @Test void sessionAllowsAccessAndLogoutRevokesIt() throws Exception {
        login();
        var me = request("GET", "/auth/me", null, false);
        assertEquals(200, me.statusCode()); assertTrue(me.body().contains("Estudante HTTP"));
        assertFalse(me.body().contains("password"));
        assertEquals(200, request("GET", "/listings", null, false).statusCode());
        assertEquals(200, request("POST", "/auth/logout", null, true).statusCode());
        assertEquals(401, request("GET", "/auth/me", null, false).statusCode());
    }
    @Test void invalidInvitationDoesNotCreateAnAccount() throws Exception {
        var result = request("POST", "/auth/register", "{\"ra\":\"20260049\",\"name\":\"Ana\",\"course\":\"Direito\",\"password\":\"Senha@123\",\"invitation\":\"invalid\"}", true);
        assertEquals(422, result.statusCode()); assertFalse(users.existsByRa("20260049"));
    }
    @Test void profileDoesNotExposeRaOrPasswordHash() throws Exception {
        login();
        var member = users.findAll().stream().filter(u -> u.name().equals("Estudante HTTP")).findFirst().orElseThrow();
        var result = request("GET", "/users/" + member.id(), null, false);
        assertEquals(200, result.statusCode());
        assertFalse(result.body().contains("\"ra\"")); assertFalse(result.body().contains("passwordHash"));
    }
    static class SocketListener implements WebSocket.Listener {
        final BlockingQueue<String> frames = new LinkedBlockingQueue<>();
        final CompletableFuture<Integer> closed = new CompletableFuture<>();
        final StringBuilder buffer = new StringBuilder();
        @Override public void onOpen(WebSocket socket) { socket.request(1); }
        @Override public CompletionStage<?> onText(WebSocket socket, CharSequence text, boolean last) {
            buffer.append(text);
            if (last) { frames.add(buffer.toString()); buffer.setLength(0); }
            socket.request(1); return CompletableFuture.completedFuture(null);
        }
        @Override public CompletionStage<?> onClose(WebSocket socket, int status, String reason) {
            closed.complete(status); return CompletableFuture.completedFuture(null);
        }
    }
    private WebSocket connect(SocketListener listener) throws Exception {
        return client.newWebSocketBuilder().header("Origin", "http://127.0.0.1:" + port)
            .buildAsync(URI.create("ws://127.0.0.1:" + port + "/ws"), listener).get(10, TimeUnit.SECONDS);
    }
    @Test void anonymousCannotConnectToWebSocket() {
        var error = assertThrows(ExecutionException.class, () -> connect(new SocketListener()));
        assertInstanceOf(WebSocketHandshakeException.class, error.getCause());
        assertEquals(401, ((WebSocketHandshakeException) error.getCause()).getResponse().statusCode());
    }
    @Test void logoutAlsoClosesAuthenticatedWebSocket() throws Exception {
        login(); var listener = new SocketListener(); var socket = connect(listener);
        socket.sendText("CONNECT\naccept-version:1.2\nhost:localhost\n\n\0", true).join();
        String frame = listener.frames.poll(10, TimeUnit.SECONDS);
        assertNotNull(frame); assertTrue(frame.startsWith("CONNECTED"));
        assertEquals(200, request("POST", "/auth/logout", null, true).statusCode());
        assertEquals(1008, listener.closed.get(10, TimeUnit.SECONDS));
    }
    @Test void subscriptionToArbitraryChannelIsRejected() throws Exception {
        login(); var listener = new SocketListener(); var socket = connect(listener);
        socket.sendText("CONNECT\naccept-version:1.2\nhost:localhost\n\n\0", true).join();
        assertNotNull(listener.frames.poll(10, TimeUnit.SECONDS));
        socket.sendText("SUBSCRIBE\nid:invalid\ndestination:/topic/private-data\n\n\0", true).join();
        String frame = listener.frames.poll(10, TimeUnit.SECONDS);
        assertNotNull(frame); assertTrue(frame.startsWith("ERROR"));
        socket.abort();
    }
}
