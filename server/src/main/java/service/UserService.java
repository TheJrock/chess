package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UserAlreadyExistsException;
import datamodel.*;
import jdk.jshell.spi.ExecutionControl;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public void clear() {dataAccess.clear();}

    public AuthData register(UserData user) throws UserAlreadyExistsException, IllegalArgumentException {
        if (user.username() == null || user.username().isBlank()) {
            throw new IllegalArgumentException("Username Required");
        }
        if (user.password() == null || user.password().isBlank()) {
            throw new IllegalArgumentException("Password Required");
        }
        if (user.email() == null || user.email().isBlank()) {
            throw new IllegalArgumentException("Email Required");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new UserAlreadyExistsException("Username Unavailable");
        }
        dataAccess.createUser(user);
        String authToken = generateAuthToken();
        return new AuthData(user.username(), authToken);
    }

    public AuthData login(String username, String password) {
        return null;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
