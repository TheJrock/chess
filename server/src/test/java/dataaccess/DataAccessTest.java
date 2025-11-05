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

}