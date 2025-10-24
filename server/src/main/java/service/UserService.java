package service;

import dataaccess.*;
import datamodel.*;
import jdk.jshell.spi.ExecutionControl;

import java.util.Objects;
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
        return new AuthData(user.username(), generateAuthToken());
    }

    public AuthData login(String username, String password) throws UnauthorizedException {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required");
        }
        var user = dataAccess.getUser(username);
        if (user == null || !user.password().equals(password)) {
            throw new UnauthorizedException("Invalid username or password");
        }
        return new AuthData(username, generateAuthToken());
    }

    public void logout(AuthData authData) throws UnauthorizedException {
        if (authData == null || dataAccess.getUser(authData.username()) == null) {
            throw new UnauthorizedException("Failed to logout nonexistent user");
        }

    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
