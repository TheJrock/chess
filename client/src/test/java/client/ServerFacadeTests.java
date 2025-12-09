package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import exception.ResponseException;
import datamodel.*;

import java.io.IOException;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void reset() throws ResponseException {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void testRegisterSuccess() throws ResponseException {
        UserData user = new UserData("testUser1", "email@test.com", "password123");

        AuthData auth = facade.register(user);

        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("testUser1", auth.username());
    }

    @Test
    public void testRegisterDuplicateUser() throws ResponseException {
        UserData user = new UserData("testUser2", "email@test.com", "pw");

        // First registration should succeed
        facade.register(user);

        // Second registration should fail
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.register(user);
        });
    }

    @Test
    public void testLoginSuccess() throws ResponseException {
        UserData user = new UserData("loginUser", "email@test.com", "mypassword");
        facade.register(user);

        AuthData auth = facade.login("loginUser", "mypassword");

        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("loginUser", auth.username());
    }

    @Test
    public void testLoginFailureWrongPassword() throws ResponseException {
        UserData user = new UserData("badLoginUser", "email@test.com", "rightPW");
        facade.register(user);

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login("badLoginUser", "wrongPW");
        });
    }

    @Test
    public void testLogoutSuccess() throws ResponseException {
        UserData user = new UserData("logoutUser", "email@test.com", "pw");
        AuthData auth = facade.register(user);
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.list(auth.authToken());
        });
    }

    @Test
    public void testCreateGameSuccess() throws ResponseException {
        UserData user = new UserData("creator", "email@test.com", "pw");
        AuthData auth = facade.register(user);
        int gameId = facade.create(auth.authToken(), "MyGame");
        Assertions.assertTrue(gameId > 0);
    }

    @Test
    public void testCreateGameNoAuth() throws ResponseException {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.create("noAuth", "MyGame");
        });
    }

    @Test
    public void testListGamesSuccess() throws ResponseException {
        UserData user = new UserData("lister", "email@test.com", "pw");
        AuthData auth = facade.register(user);
        facade.create(auth.authToken(), "GameA");
        facade.create(auth.authToken(), "GameB");
        GameData[] games = facade.list(auth.authToken());
        Assertions.assertTrue(games.length >= 2);
    }

    @Test
    public void testListGamesInvalidToken() throws ResponseException {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.list("noAuth");
        });
    }

    @Test
    public void testJoinGameSuccess() throws ResponseException {
        UserData user = new UserData("joiner", "email@test.com", "pw");
        AuthData auth = facade.register(user);
        int gameId = facade.create(auth.authToken(), "JoinableGame");
        Assertions.assertDoesNotThrow(() -> {
            facade.join(auth.authToken(), "WHITE", gameId);
        });
    }

    @Test
    public void testJoinGameInvalidToken() throws ResponseException {
        UserData user = new UserData("joiner2", "email@test.com", "pw");
        AuthData auth = facade.register(user);
        int gameId = facade.create(auth.authToken(), "JoinableGame2");
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.join("BadToken", "BLACK", gameId);
        });
    }

    @Test
    public void testJoinGameInvalidTeam() throws ResponseException {
        UserData user = new UserData("joiner3", "email@test.com", "pw");
        AuthData auth = facade.register(user);
        int gameId = facade.create(auth.authToken(), "JoinableGame3");
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.join(auth.authToken(), "NOT_A_TEAM", gameId);
        });
    }
}
