package ui;

import datamodel.UserData;
import exception.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

public class LoggedOutClient implements Client {
    private final ServerFacade facade;

    public LoggedOutClient(ServerFacade facade) {
        this.facade = facade;
    }

    @Override
    public void eval(String input, Repl repl) {
        String[] tokens = input.toLowerCase().split(" ");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        switch (cmd) {
            case "login" -> login(params, repl);
            case "register" -> register(params, repl);
            case "quit" -> quit(repl);
            default -> help();
        }
    }

    private void login(String[] params, Repl repl) {
        if (params.length != 2) {
            System.err.println("Invalid login command");
            return;
        }
        String username = params[0];
        String password = params[1];
        try {
            var authData = facade.login(username, password);
            repl.setClient(new LoggedInClient(facade, authData.authToken()));
            System.out.println("Logged in as " + username);
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void register(String[] params, Repl repl) {
        if (params.length != 3) {
            System.err.println("Invalid register command");
            return;
        }
        UserData userData = new UserData(params[0], params[1], params[2]);
        try {
            var authData = facade.register(userData);
            repl.setClient(new LoggedInClient(facade, authData.authToken()));
            System.out.println("Registered as " + userData.username());
        } catch (Exception e) {
            System.out.println("Register failed: " + e.getMessage());
        }
    }

    @Override
    public void quit(Repl repl) {
        System.out.println("Farewell!");
        System.exit(0);
    }

    @Override
    public void help() {
        System.out.println("""
                Commands:\s
                register <USERNAME> <EMAIL> <PASSWORD> - to login as a new user\s
                login <USERNAME> <PASSWORD> - to login as an existing user\s
                quit - to exit the program\s
                help - to show all available commands\s
                [LOGGED OUT] >>>\s""");
    }
}
