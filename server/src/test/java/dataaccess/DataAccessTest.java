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
        assert user.equals(new UserData("username", "email", "password"));
    }

    @Test
    void getNonExistingUser() {
        assertNull(dataAccess.getUser("username"), "user should not be found");
    }

    @Test
    void createAuthToken() throws DataAccessException {
        var user = new UserData("username", "email", "password");
        dataAccess.createUser(user);
        var auth = new AuthData("token123", user.username());
        dataAccess.createAuth(auth);
        var retrieved = dataAccess.getAuth("token123");
        assertNotNull(retrieved);
        assertEquals("username", retrieved.username());
        assertEquals("token123", retrieved.authToken());
    }

    @Test
    void createAuthTokenDuplicate() throws DataAccessException {
        var user = new UserData("user", "email", "password");
        dataAccess.createUser(user);
        var auth = new AuthData("tokenABC", user.username());
        dataAccess.createAuth(auth);
        var ex = assertThrows(Exception.class, () -> dataAccess.createAuth(auth));
        assertEquals("Database error while creating auth", ex.getMessage());
    }

    @Test
    void createGame() throws DataAccessException {
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
    void getNonexistentGame() throws DataAccessException {
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
        assertEquals("Failed to connect to database", ex.getMessage());
    }

    @Test
    void getGames() throws DataAccessException {
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
}