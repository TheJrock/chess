package dataaccess;

import datamodel.*;

import java.util.HashMap;

public interface DataAccess {
    void createUser(UserData user);
    UserData getUser(String username);
    void deleteUser(String username);
    void createAuth(AuthData authData);
    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);
    String createGame(GameData gameData);
    GameData getGame(String gameId);
    HashMap<String, GameData> getGames();

    void clear();
}
