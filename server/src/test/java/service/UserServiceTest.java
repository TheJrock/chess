package service;

import dataaccess.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class UserServiceTest {

    private UserService userService;
    private DataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new MysqlDataAccess();
        dataAccess.clear();
        userService = new UserService(dataAccess);
    }

    @Test
    void registerUser() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        assertNotNull(authData, "authData should not be null");
        assertEquals("john", authData.username(), "username should be john");
        assertNotNull(authData.authToken(), "Auth Token should be generated");
    }

    @Test
    void registerDuplicateUser() throws Exception {
        var user1 = new UserData("john", "john@example.com", "password123");
        var user2 = new UserData("john", "johnny@example.com", "differentPassword123");
        userService.register(user1);
        var ex = assertThrows(Exception.class, () -> userService.register(user2));
        assertEquals("Username Unavailable", ex.getMessage());
    }

    @Test
    void registerMissingPassword() {
        var user = new UserData("john", "john@example.com", null);
        var ex = assertThrows(Exception.class, () -> userService.register(user));
        assertEquals("Password Required", ex.getMessage());
    }

    @Test
    void registerMissingEmail() {
        var user = new UserData("john", null, "password123");
        var ex = assertThrows(Exception.class, () -> userService.register(user));
        assertEquals("Email Required", ex.getMessage());
    }

    @Test
    void registerMissingUsername() {
        var user = new UserData(null, "john@example.com", "password123");
        var ex = assertThrows(Exception.class, () -> userService.register(user));
        assertEquals("Username Required", ex.getMessage());
    }

    @Test
    void loginUser() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        userService.register(user);
        var authData = userService.login("john", "password123");
        assertNotNull(authData, "authData should not be null");
    }

    @Test
    void loginInvalidUser() {
        var ex = assertThrows(Exception.class, () -> userService.login("john", "password123"));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void loginCorrectUsernameWrongPassword() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        userService.register(user);
        var ex = assertThrows(Exception.class, () -> userService.login("john", "wrongPassword123"));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void loginCorrectPasswordWrongUsername() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        userService.register(user);
        var ex = assertThrows(Exception.class, () -> userService.login("johnny", "password123"));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void loginWithoutPassword() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        userService.register(user);
        var ex = assertThrows(Exception.class, () -> userService.login("johnny", ""));
        assertEquals("Username and password are required", ex.getMessage());
    }

    @Test
    void loginWithoutUsername() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        userService.register(user);
        var ex = assertThrows(Exception.class, () -> userService.login("", "password123"));
        assertEquals("Username and password are required", ex.getMessage());
    }

    @Test
    void logoutUser() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        userService.register(user);
        var authData = userService.login("john", "password123");
        userService.logout(authData.authToken());
    }

    @Test
    void logoutInvalidUser() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        userService.logout(authData.authToken());
        var ex = assertThrows(Exception.class, () -> userService.logout(authData.authToken()));
        assertEquals("Failed to logout nonexistent user", ex.getMessage());
    }

    @Test
    void createGame() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        var gameID = userService.create(authData.authToken(), "john's game");
        assertEquals("john's game", dataAccess.getGame(gameID).gameName());
    }

    @Test
    void createGameInvalidAuthToken() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        userService.logout(authData.authToken());
        var ex = assertThrows(Exception.class, () -> userService.create(authData.authToken(), "john's game"));
        assertEquals("Failed to authorize user", ex.getMessage());
    }

    @Test
    void createGameInvalidName() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        var ex = assertThrows(Exception.class, () -> userService.create(authData.authToken(), ""));
        assertEquals("Game Name Required", ex.getMessage());
    }

    @Test
    void listGames() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        var gameID = userService.create(authData.authToken(), "john's game");
        HashMap<Integer, GameData> expected = new HashMap<>();
        GameData game = new GameData(gameID, null, null, "john's game");
        expected.put(gameID, game);
        var games = userService.list(authData.authToken());
        assertEquals(expected, games);
    }

    @Test
    void listGamesEmpty() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        HashMap<Integer, GameData> expected = new HashMap<>();
        var games = userService.list(authData.authToken());
        assertEquals(expected, games);
    }

    @Test
    void listGamesInvalidAuthToken() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        userService.logout(authData.authToken());
        var ex = assertThrows(Exception.class, () -> userService.list(authData.authToken()));
        assertEquals("Failed to authorize user", ex.getMessage());
    }

    @Test
    void joinGameAsWhite() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        var gameID = userService.create(authData.authToken(), "john's game");
        userService.join(authData.authToken(), "WHITE", gameID);
        assertEquals("john", dataAccess.getGame(gameID).whiteUsername());
    }

    @Test
    void joinGameAsBlack() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        var gameID = userService.create(authData.authToken(), "john's game");
        userService.join(authData.authToken(), "BLACK", gameID);
        assertEquals("john", dataAccess.getGame(gameID).blackUsername());
    }

    @Test
    void joinGameTwoPlayers() throws Exception {
        var user1 = new UserData("john", "john@example.com", "password123");
        var user2 = new UserData("Johnny", "Johnny@example.com", "otherPassword123");
        var authData1 = userService.register(user1);
        var authData2 = userService.register(user2);
        var gameID = userService.create(authData1.authToken(), "john's game");
        userService.join(authData1.authToken(), "WHITE", gameID);
        userService.join(authData2.authToken(), "BLACK", gameID);
        assertEquals("john", dataAccess.getGame(gameID).whiteUsername());
        assertEquals("Johnny", dataAccess.getGame(gameID).blackUsername());
    }

    @Test
    void joinGameInvalidAuthToken() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        var gameID = userService.create(authData.authToken(), "john's game");
        userService.logout(authData.authToken());
        var ex = assertThrows(Exception.class, () -> userService.join(authData.authToken(), "WHITE", gameID));
        assertEquals("Failed to authorize user", ex.getMessage());
    }

    @Test
    void joinGameInvalidId() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        var gameID = userService.create(authData.authToken(), "john's game");
        var wrongGameID = gameID + 1;
        var ex = assertThrows(Exception.class, () -> userService.join(authData.authToken(), "WHITE", wrongGameID));
        assertEquals("Game not found", ex.getMessage());
    }

    @Test
    void joinGameInvalidTeam() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        var gameID = userService.create(authData.authToken(), "john's game");
        var ex = assertThrows(Exception.class, () -> userService.join(authData.authToken(), "Winning Team", gameID));
        assertEquals("Unsupported team", ex.getMessage());
    }

    @Test
    void joinGameWhiteTaken() throws Exception {
        var user1 = new UserData("john", "john@example.com", "password123");
        var user2 = new UserData("Johnny", "Johnny@example.com", "otherPassword123");
        var authData1 = userService.register(user1);
        var authData2 = userService.register(user2);
        var gameID = userService.create(authData1.authToken(), "john's game");
        userService.join(authData1.authToken(), "WHITE", gameID);
        var ex = assertThrows(Exception.class, () -> userService.join(authData2.authToken(), "WHITE", gameID));
        assertEquals("Spot already taken", ex.getMessage());
    }

    @Test
    void joinGameBlackTaken() throws Exception {
        var user1 = new UserData("john", "john@example.com", "password123");
        var user2 = new UserData("Johnny", "Johnny@example.com", "otherPassword123");
        var authData1 = userService.register(user1);
        var authData2 = userService.register(user2);
        var gameID = userService.create(authData1.authToken(), "john's game");
        userService.join(authData1.authToken(), "BLACK", gameID);
        var ex = assertThrows(Exception.class, () -> userService.join(authData2.authToken(), "BLACK", gameID));
        assertEquals("Spot already taken", ex.getMessage());
    }

    @Test
    void clear() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        userService.register(user);
        assertNotNull(dataAccess.getUser("john"), "user should exist before clear");
        userService.clear();
        assertNull(dataAccess.getUser("john"), "user should not exist after clear");
    }
}