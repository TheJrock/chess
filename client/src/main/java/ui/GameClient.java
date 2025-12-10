package ui;

import server.ServerFacade;

public class GameClient implements Client {

    private final ServerFacade facade;
    private final String authToken;
    private final int gameID;

    public GameClient(ServerFacade facade, String authToken, int gameID, String teamColor) {
        this.facade = facade;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    @Override
    public void quit(Repl repl) {

    }

    @Override
    public void help() {
        return;
    }

    @Override
    public void eval(String input, Repl repl) {
        return;
    }
}
