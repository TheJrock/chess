package dataaccess;

import datamodel.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    private DataAccess dataAccess;
    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
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
    void getUser() {
        dataAccess.createUser(new UserData("username", "email", "password"));
        UserData user = dataAccess.getUser("username");
        assert user.equals(new UserData("username", "email", "password"));
    }

    @Test
    void getNonExistingUser() {
        assertNull(dataAccess.getUser("username"), "user should not be found");
    }
}