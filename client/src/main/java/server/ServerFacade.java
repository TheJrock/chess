package server;

import datamodel.*;

public class ServerFacade {

    public ServerFacade() {

    }

    public AuthData register(UserData userData) {}

    public AuthData login(String username, String password) throws UnauthorizedException {}

    public void logout(String authToken) {}

    public int create(String authToken, String gameName) throws UnauthorizedException {}

    public void join(String authToken, String team, int gameID) throws UnauthorizedException {}
}
