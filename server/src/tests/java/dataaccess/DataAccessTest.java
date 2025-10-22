package dataaccess;

import datamodel.UserData;
import org.junit.jupiter.api.Test;

class DataAccessTest {

    @Test
    void clear() {
        DataAccess dataAccess = new MemoryDataAccess();
        dataAccess.createUser(new UserData("username", "email", "password"));
        dataAccess.clear();
        assert dataAccess.getUser("username") == null;
    }

    @Test
    void createUser() {
        DataAccess dataAccess = new MemoryDataAccess();
        dataAccess.createUser(new UserData("username", "email", "password"));
        UserData user = dataAccess.getUser("username");
        assert user.equals(new UserData("username", "email", "password"));
    }

    @Test
    void getUser() {
        DataAccess dataAccess = new MemoryDataAccess();
        dataAccess.createUser(new UserData("username", "email", "password"));
        UserData user = dataAccess.getUser("username");
        assert user.equals(new UserData("username", "email", "password"));
    }
}