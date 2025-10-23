package server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.http.*;
import java.net.*;
import java.io.IOException;
import com.google.gson.Gson;
import java.util.Map;

class ServerTest {

    private static Server server;
    private static int port;
    private static final Gson gson = new Gson();
    private static HttpClient client;

    @BeforeAll
    static void startServer() {
        server = new Server();
        port = server.run(0); // 0 = random free port
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void testClearDatabase() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/db"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("{}", response.body());
    }

    @Test
    void testRegisterUserSuccess() throws IOException, InterruptedException {
        Map<String, String> userMap = Map.of(
                "username", "alice",
                "password", "password123",
                "email", "alice@example.com"
        );
        String json = gson.toJson(userMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Map<?, ?> resp = gson.fromJson(response.body(), Map.class);
        assertEquals("alice", resp.get("username"));
        assertNotNull(resp.get("authToken"));
    }

    @Test
    void testRegisterUserMissingField() throws IOException, InterruptedException {
        Map<String, String> userMap = Map.of(
                "username", "",
                "password", "password123",
                "email", "alice@example.com"
        );
        String json = gson.toJson(userMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        Map<?, ?> resp = gson.fromJson(response.body(), Map.class);
        assertTrue(((String) resp.get("message")).toLowerCase().contains("bad request"));
    }

    @Test
    void testRegisterUserAlreadyExists() throws IOException, InterruptedException {
        // Register once
        Map<String, String> userMap = Map.of(
                "username", "bob",
                "password", "password123",
                "email", "bob@example.com"
        );
        String json = gson.toJson(userMap);

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        client.send(request1, HttpResponse.BodyHandlers.ofString());

        // Register again -> should fail
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response2.statusCode());
        Map<?, ?> resp = gson.fromJson(response2.body(), Map.class);
        assertTrue(((String) resp.get("message")).toLowerCase().contains("already taken"));
    }
}
