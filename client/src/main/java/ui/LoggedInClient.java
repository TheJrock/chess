package ui;

import datamodel.GameData;
import server.ServerFacade;

public class LoggedInClient implements Client {

    private final ServerFacade facade;
    private final String authToken;
    private GameData[]  gameDataSet;

    public LoggedInClient(ServerFacade facade, String authToken) {
        this.facade = facade;
        this.authToken = authToken;
        try {
            gameDataSet = facade.list(authToken);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void quit(Repl repl) {
        System.out.println("Farewell!");
        logout(repl);
        System.exit(0);
    }

    @Override
    public void help() {
        System.out.println("""
                Commands:\s
                create <NAME> - to create a game\s
                list - to list all games\s
                join <ID> [WHITE|BLACK] - to join a game as a player\s
                observe <ID> - to watch a game\s
                quit - to exit the program\s
                help - to show all available commands\s""");
    }

    @Override
    public void eval(String input, Repl repl) {
        String[] tokens = input.split(" ", 2);
        String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
        String params = (tokens.length > 1) ? tokens[1] : "";
        switch (cmd) {
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "join" -> joinGame(params, repl);
            case "observe" -> observeGame(params, repl);
            case "logout" -> logout(repl);
            case "quit" -> quit(repl);
            default -> help();
        }
    }

    private void logout(Repl repl) {
        try {
            facade.logout(authToken);
            repl.setClient(new LoggedOutClient(facade));
            System.out.println("Thanks for playing!");
        } catch (Exception e) {
            System.err.println("Oops, there was an error logging out: " + e.getMessage());
        }
    }

    private void observeGame(String params, Repl repl) {
        if (params.isBlank()) {
            System.err.println("Invalid observe command. Type help for valid command patterns.");
            return;
        }
        int gameID;
        try {
            gameID = Integer.parseInt(params.trim());
        } catch (NumberFormatException e) {
            System.err.println("Game ID must be a valid integer. Type help for valid command patterns.");
            return;
        }
        if (gameID < 1 || gameID > gameDataSet.length) {
            System.err.println("No game exists with game ID: " + gameID + ". Type list for a list of valid games.");
            return;
        }
        GameData game = gameDataSet[gameID - 1];
        try {
            facade.join(authToken, null, game.gameID());
            System.out.println("You are now observing " + game.gameName() + "!");
            System.out.println(BoardRenderer.renderInitial(true));
//            repl.setClient(new GameClient(facade, authToken, game.gameID(), null));
        } catch (Exception e) {
            System.err.println("Something went wrong when observing game: " + e.getMessage());
        }
    }

    private void joinGame(String arg, Repl repl) {
        String[] params = arg.split(" ", 2);
        if (params.length != 2) {
            System.err.println("Invalid join command. Type help for valid command patterns.");
            return;
        }
        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            System.err.println("Game ID must be a valid integer. Type help for valid command patterns.");
            return;
        }
        if (gameID < 1 || gameID > gameDataSet.length) {
            System.err.println("No game exists with game ID: " + gameID + ". Type list for a list of valid games.");
            return;
        }
        String teamColor = params[1].toUpperCase();
        if (!teamColor.equals("WHITE") && !teamColor.equals("BLACK")) {
            System.err.println("Invalid team color: " + teamColor + ". Type help for valid command patterns.");
        }
        GameData game =  gameDataSet[gameID-1];
        try {
            facade.join(authToken, teamColor, game.gameID());
            System.out.println("Joined " + game.gameName() + " as " + teamColor + "!");
            System.out.println(BoardRenderer.renderInitial(teamColor.equals("WHITE")));
//            repl.setClient(new GameClient(facade, authToken, game.gameID(), teamColor));
        } catch (Exception e) {
            System.err.println("Something went wrong when joining game: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            gameDataSet = facade.list(authToken);
            int gameID = 1;
            System.out.println("Game ID | Game Name");
            for (GameData gameData : gameDataSet) {
                System.out.println(gameID++ + " | " + gameData.gameName());
            }
        } catch (Exception e) {
            System.err.println("Something went wrong when listing games: " + e.getMessage());
        }
    }

    private void createGame(String gameName) {
        if (gameName.isBlank()) {
            System.err.println("Invalid create command. Type help for valid command patterns.");
            return;
        }
        try {
            facade.create(authToken, gameName);
            gameDataSet = facade.list(authToken);
            System.out.println("Created game " + gameName + "!");
        } catch (Exception e) {
            System.err.println("Something went wrong when creating game: " + e.getMessage());
        }
    }
}
