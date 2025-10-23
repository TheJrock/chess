package service;

import dataaccess.DataAccess;
import datamodel.*;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public void clear() {dataAccess.clear();}

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.username().isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (user.email() == null || user.email().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (user.password() == null || user.password().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("Username already exists");
        }
        dataAccess.createUser(user);
        var authToken = generateAuthToken();
        return new AuthData(user.username(), authToken);
    }
    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
