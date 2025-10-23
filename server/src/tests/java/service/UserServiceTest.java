package service;

import dataaccess.DataAccess;
import datamodel.AuthData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Test;
import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private DataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
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
        var user2 = new UserData("john", "different@example.com", "differentPassword123");
        userService.register(user1);
        var ex = assertThrows(Exception.class, () -> userService.register(user2));
        assertTrue(ex.getMessage().toLowerCase().contains("username"), "Error message should mention username");
    }

    @Test
    void registerMissingPassword() {
        var user = new UserData("john", "john@example.com", null);
        var ex = assertThrows(Exception.class, () -> userService.register(user));
        assertTrue(ex.getMessage().toLowerCase().contains("password"), "Error message should mention password");
    }

    @Test
    void registerMissingEmail() {
        var user = new UserData("john", null, "pasword123");
        var ex = assertThrows(Exception.class, () -> userService.register(user));
        assertTrue(ex.getMessage().toLowerCase().contains("email"), "Error message should mention email");
    }

    @Test
    void registerMissingUsername() {
        var user = new UserData(null, "john@example.com", "password123");
        var ex = assertThrows(Exception.class, () -> userService.register(user));
        assertTrue(ex.getMessage().toLowerCase().contains("username"), "Error message should mention username");
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