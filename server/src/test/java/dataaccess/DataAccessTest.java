package dataaccess;

import datamodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    private DataAccess dataAccess;
    @BeforeEach
    void setUp() {
        dataAccess = new MysqlDataAccess();
        dataAccess.clear();
    }

    @Test
    void clear() {
        dataAccess.createUser(new UserData("username", "email", "password"));
        dataAccess.clear();
        assertNull(dataAccess.getUser("username"), "User data should be null");
    }

    @Test
    void createUser() {
        dataAccess.createUser(new UserData("username", "email", "password"));
        UserData user = dataAccess.getUser("username");
        assertNotNull(user, "user should not be null");
        assertEquals("username", user.username());
        assertEquals("email", user.email());
        assertEquals("password", user.password());
    }

    @Test
    void createDuplicateUser() {
        dataAccess.createUser(new UserData("username", "email", "password"));
        var ex = assertThrows(Exception.class, () -> dataAccess.createUser(new UserData("username", "differentEmail", "otherPassword")));
        assertEquals("Username Unavailable", ex.getMessage());
    }

    @Test
    void getUser() {
        dataAccess.createUser(new UserData("username", "email", "password"));
        UserData user = dataAccess.getUser("username");
        assertEquals(new UserData("username", "email", "password"), user);
    }

    @Test
    void getNonExistingUser() {
        assertNull(dataAccess.getUser("username"), "user should not be found");
    }

    @Test
    void createAuthToken() {
        var user = new UserData("username", "email", "password");
        dataAccess.createUser(user);
        var auth = new AuthData("token123", user.username());
        dataAccess.createAuth(auth);
        var retrieved = dataAccess.getAuth("token123");
        assertEquals("username", retrieved.username());
        assertEquals("token123", retrieved.authToken());
    }

    @Test
    void createAuthTokenDuplicate() {
        var user = new UserData("user", "email", "password");
        dataAccess.createUser(user);
        var auth = new AuthData("tokenABC", user.username());
        dataAccess.createAuth(auth);
        var ex = assertThrows(Exception.class, () -> dataAccess.createAuth(auth));
        assertEquals("Database error while creating auth", ex.getMessage());
    }

    @Test
    void getAuthToken() {
        var user = new UserData("username", "email", "password");
        dataAccess.createUser(user);
        var auth = new AuthData("token123", user.username());
        dataAccess.createAuth(auth);
        var retrieved = dataAccess.getAuth("token123");
        assertNotNull(retrieved);
    }

    @Test
    void getAuthTokenInvalid() {
        assertNull(dataAccess.getAuth("token123"));
    }

    @Test
    void deleteAuthToken() {
        var user = new UserData("username", "email", "password");
        dataAccess.createUser(user);
        var auth = new AuthData("token123", user.username());
        dataAccess.createAuth(auth);
        dataAccess.deleteAuth("token123");
        assertNull(dataAccess.getAuth("token123"));
    }

    @Test
    void createGame() {
        var user1 = new UserData("whiteUser", "email", "password");
        var user2 = new UserData("blackUser", "email", "password");
        dataAccess.createUser(user1);
        dataAccess.createUser(user2);
        var game = new GameData(1, "whiteUser", "blackUser", "Friendly Match");
        var gameID = dataAccess.createGame(game);

        var retrieved = dataAccess.getGame(gameID);

        assertNotNull(retrieved);
        assertEquals("whiteUser", retrieved.whiteUsername());
        assertEquals("blackUser", retrieved.blackUsername());
        assertEquals("Friendly Match", retrieved.gameName());
    }

    @Test
    void createGameBadUsers() {
        var ex = assertThrows(Exception.class, () -> dataAccess.createGame(new GameData(1, "whiteUser", "blackUser", "Friendly Match")));
        assertEquals("Database error while creating game", ex.getMessage());
    }

    @Test
    void getGame() {
        var user = new UserData("whiteUser", "email", "password");
        var user2 = new UserData("blackUser", "email2", "password2");
        dataAccess.createUser(user);
        dataAccess.createUser(user2);
        var game = new GameData(1, "whiteUser", "blackUser", "Friendly Match");
        var gameID = dataAccess.createGame(game);
        var retrieved = dataAccess.getGame(gameID);
        assertNotNull(retrieved);
        assertEquals("whiteUser", retrieved.whiteUsername());
        assertEquals("blackUser", retrieved.blackUsername());
        assertEquals("Friendly Match", retrieved.gameName());
    }

    @Test
    void getNonexistentGame() {
        var result = dataAccess.getGame(999);
        assertNull(result);
    }

    @Test
    void updateGame() throws DataAccessException {
        UserData white = new UserData("alice", "email@example.com", "password");
        UserData black = new UserData("bob", "email2@example.com", "password");

        dataAccess.createUser(white);
        dataAccess.createUser(black);

        var game = new GameData(0, "alice", "bob", "Game A");
        int gameID = dataAccess.createGame(game);

        dataAccess.createUser(new UserData("charlie", "email3@example.com", "password"));
        var updated = new GameData(gameID, "alice", "charlie", "Updated Game A");
        dataAccess.updateGame(updated);

        var retrieved = dataAccess.getGame(gameID);
        assertEquals("charlie", retrieved.blackUsername());
        assertEquals("Updated Game A", retrieved.gameName());
    }

    @Test
    void updateNonexistentGame() {
        var game = new GameData(42, "user1", "user2", "Ghost Game");
        var ex = assertThrows(Exception.class, () -> dataAccess.updateGame(game));
        assertEquals("Game not found", ex.getMessage());
    }

    @Test
    void getGames() {
        var u1 = new UserData("u1", "email@example.com", "password");
        var u2 = new UserData("u2", "email2@example.com", "password");
        var u3 = new UserData("u3", "email3@example.com", "password");
        var u4 = new UserData("u4", "email4@example.com", "password");
        dataAccess.createUser(u1);
        dataAccess.createUser(u2);
        dataAccess.createUser(u3);
        dataAccess.createUser(u4);
        var gameID1 = dataAccess.createGame(new GameData(1, "u1", "u2", "Game One"));
        var gameID2 = dataAccess.createGame(new GameData(2, "u3", "u4", "Game Two"));

        var games = dataAccess.getGames();
        assertEquals(2, games.size());
        assertTrue(games.containsKey(gameID1));
        assertTrue(games.containsKey(gameID2));
    }

    @Test
    void createGameWithNoPlayers() {
        var game = new GameData(0, null, null, "Solo Game");
        int gameID = dataAccess.createGame(game);
        var retrieved = dataAccess.getGame(gameID);
        assertNotNull(retrieved);
        assertNull(retrieved.whiteUsername());
        assertNull(retrieved.blackUsername());
        assertEquals("Solo Game", retrieved.gameName());
    }

    @Test
    void duplicateAuthTokenDifferentUsersFails() {
        var user1 = new UserData("user1", "email1", "pw");
        var user2 = new UserData("user2", "email2", "pw");
        dataAccess.createUser(user1);
        dataAccess.createUser(user2);

        dataAccess.createAuth(new AuthData("sharedToken", "user1"));
        var ex = assertThrows(Exception.class, () -> dataAccess.createAuth(new AuthData("sharedToken", "user2")));
        assertEquals("Database error while creating auth", ex.getMessage());
    }

    @Test
    void clearDeletesUsersAuthsAndGames() {
        var user = new UserData("u", "email", "pw");
        dataAccess.createUser(user);
        dataAccess.createAuth(new AuthData("t", "u"));
        dataAccess.createGame(new GameData(0, "u", null, "Game"));

        dataAccess.clear();

        assertTrue(dataAccess.getGames().isEmpty(), "Games should be cleared");
        assertNull(dataAccess.getAuth("t"), "Auth should be cleared");
        assertNull(dataAccess.getUser("u"), "User should be cleared");
    }
}