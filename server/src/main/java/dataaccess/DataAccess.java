package dataaccess;

import java.util.HashMap;
import datamodel.*;

public interface DataAccess {
    void createUser(UserData user);

    UserData getUser(String username);

    void createAuth(AuthData authData);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken);

    int createGame(GameData gameData);

    GameData getGame(int gameID);

    void updateGame(GameData gameData) throws DataAccessException;
    
    HashMap<Integer, GameData> getGames();

    void clear();
}
