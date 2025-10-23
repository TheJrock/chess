package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UserAlreadyExistsException;
import datamodel.*;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public void clear() {dataAccess.clear();}

    public AuthData register(UserData user) throws UserAlreadyExistsException, IllegalArgumentException, DataAccessException {
        if (user.username() == null || user.username().isBlank()) {
            throw new IllegalArgumentException("Missing username");
        }
        if (user.password() == null || user.password().isBlank()) {
            throw new IllegalArgumentException("Missing password");
        }
        if (user.email() == null || user.email().isBlank()) {
            throw new IllegalArgumentException("Missing email");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        dataAccess.createUser(user);
        String authToken = generateAuthToken();
        return new AuthData(user.username(), authToken);
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
