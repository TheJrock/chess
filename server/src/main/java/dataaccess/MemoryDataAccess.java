package dataaccess;

import datamodel.*;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, AuthData> authTokens = new HashMap<>();
    private final HashMap<String, GameData> games = new HashMap<>();

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void deleteUser(String username) {
        users.remove(username);
    }

    @Override
    public void createAuth(AuthData authData) {
        authTokens.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    @Override
    public String createGame(GameData gameData) {
        games.put(gameData.gameID(), gameData);
        return gameData.gameID();
    }

    @Override
    public GameData getGame(String gameID) {
        return games.get(gameID);
    }

    @Override
    public void updateGame(GameData gameData) {
        games.put(gameData.gameID(), gameData);
    }

    @Override
    public HashMap<String, GameData> getGames() {
        return new HashMap<>(games);
    }

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
    }
}
