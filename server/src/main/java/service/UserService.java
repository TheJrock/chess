package service;

import dataaccess.*;
import datamodel.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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

    public int create(String authToken, String gameName) throws UnauthorizedException {
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game Name Required");
        }
        if (authToken == null || authToken.isEmpty() || dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Failed to authorize user");
        }
        return dataAccess.createGame(new GameData(0, null, null, gameName));
    }

    public HashMap<Integer, GameData> list(String authToken) throws UnauthorizedException {
        if (authToken == null || authToken.isEmpty() || dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Failed to authorize user");
        }
        return dataAccess.getGames();
    }

    public void join(String authToken, String team, int gameID) throws UnauthorizedException, DataAccessException {
        if (authToken == null || authToken.isEmpty() || dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Failed to authorize user");
        }
        if (gameID < 1) {
            throw new IllegalArgumentException("Missing or invalid game ID");
        }
        if (dataAccess.getGame(gameID) == null) {
            throw new DataAccessException("Game not found");
        }
        GameData oldGame = dataAccess.getGame(gameID);
        String username = dataAccess.getAuth(authToken).username();
        GameData newGame = updateGame(team, oldGame, username);
        dataAccess.updateGame(newGame);
    }

    @NotNull
    private static GameData updateGame(String team, GameData oldGame, String username) throws DataAccessException {
        String white = oldGame.whiteUsername();
        String black = oldGame.blackUsername();
        if (team == null || team.isBlank()) {
            throw new IllegalArgumentException("Unsupported team");
        }
        switch (team.toUpperCase()) {
            case "WHITE" -> {
                if (white != null) throw new DataAccessException("Spot already taken");
                white = username;
            }
            case "BLACK" -> {
                if (black != null) throw new DataAccessException("Spot already taken");
                black = username;
            }
            case "WHITE/BLACK" -> {
                // Join as observer
            }
            default -> throw new IllegalArgumentException("Unsupported team");
        }
        return new GameData(oldGame.gameID(), white, black, oldGame.gameName());
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
