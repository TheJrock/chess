package service;

import dataaccess.DataAccess;
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
        var user = new UserData("john", null, "pasword123");
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
        userService.logout(authData);
    }

    @Test
    void logoutInvalidUser() throws Exception {
        var user = new UserData("john", "john@example.com", "password123");
        var authData = userService.register(user);
        userService.logout(authData);
        var ex = assertThrows(Exception.class, () -> userService.logout(authData));
        assertEquals("Failed to logout nonexistent user", ex.getMessage());
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