package dataaccess;

import datamodel.*;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {

    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, AuthData> authTokens = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
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
    public int createGame(GameData gameData) {
        int gameID = nextGameID++;
        games.put(gameID, new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName()));
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void updateGame(GameData gameData) {
        games.put(gameData.gameID(), gameData);
    }

    @Override
    public HashMap<Integer, GameData> getGames() {
        return new HashMap<>(games);
    }

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
    }
}
