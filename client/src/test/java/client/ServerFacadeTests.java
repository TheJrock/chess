package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import exception.ResponseException;
import datamodel.*;

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

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void testRegisterSuccess() throws ResponseException {
        UserData user = new UserData("testUser1", "password123", "email@test.com");

        AuthData auth = facade.register(user);

        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("testUser1", auth.username());
    }

    @Test
    public void testRegisterDuplicateUser() throws ResponseException {
        UserData user = new UserData("testUser2", "pw", "email@test.com");

        // First registration should succeed
        facade.register(user);

        // Second registration should fail
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.register(user);
        });
    }

    @Test
    public void testLoginSuccess() throws ResponseException {
        UserData user = new UserData("loginUser", "mypassword", "email@test.com");
        facade.register(user);

        AuthData auth = facade.login("loginUser", "mypassword");

        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("loginUser", auth.username());
    }

    @Test
    public void testLoginFailureWrongPassword() throws ResponseException {
        UserData user = new UserData("badLoginUser", "rightPW", "email@test.com");
        facade.register(user);

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login("badLoginUser", "wrongPW");
        });
    }
}
