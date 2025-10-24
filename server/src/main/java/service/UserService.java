package service;

import dataaccess.*;
import datamodel.*;
import io.javalin.http.BadRequestResponse;

import java.util.HashMap;
import java.util.List;
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
        AuthData authData = new AuthData(user.username(), generateAuthToken());
        dataAccess.createUser(user);
        dataAccess.createAuth(authData);
        return authData;
    }

    public AuthData login(String username, String password) throws UnauthorizedException {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required");
        }
        var user = dataAccess.getUser(username);
        if (user == null || !user.password().equals(password)) {
            throw new UnauthorizedException("Invalid username or password");
        }
        AuthData authData = new AuthData(user.username(), generateAuthToken());
        dataAccess.createAuth(authData);
        return authData;
    }

    public void logout(String authToken) throws UnauthorizedException {
        if (authToken == null || authToken.isEmpty() || dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Failed to logout nonexistent user");
        }
        dataAccess.deleteAuth(authToken);
    }

    public String create(String authToken, String gameName) throws UnauthorizedException {
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game Name Required");
        }
        if (authToken == null || authToken.isEmpty() || dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Failed to authorize user");
        }
        return dataAccess.createGame(new GameData(generateGameId(), null, null, gameName));
    }

    public HashMap<String, GameData> list(String authToken) throws UnauthorizedException {
        if (authToken == null || authToken.isEmpty() || dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Failed to authorize user");
        }
        return dataAccess.getGames();
    }

    public void join(String authToken, String team, String gameId) throws UnauthorizedException, DataAccessException {
        if (authToken == null || authToken.isEmpty() || dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Failed to authorize user");
        }
        if (gameId == null || gameId.isBlank() || dataAccess.getGame(gameId) == null) {
            throw new DataAccessException("Game not found");
        }
        GameData oldGame = dataAccess.getGame(gameId);
        String username = dataAccess.getAuth(authToken).username();
        if (team.equals("WHITE")) {
            GameData newGame = new GameData(oldGame.gameId(), username, oldGame.blackUsername(), oldGame.gameName());
        } else if (team.equals("BLACK")) {
            GameData newGame = new GameData(oldGame.gameId(), oldGame.whiteUsername(), username, oldGame.gameName());
        } else {
            throw new DataAccessException("Unsupported team");
        }
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private String generateGameId() {
        return UUID.randomUUID().toString();
    }
}
