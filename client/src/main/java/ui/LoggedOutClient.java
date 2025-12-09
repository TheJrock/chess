package ui;

import exception.ResponseException;

import java.util.Arrays;

public class LoggedOutClient implements Client {
    public State state;
    private String serverUrl;
    private Repl repl;

    public LoggedOutClient(String serverUrl, Repl repl) {
        state = State.LOGGED_OUT;
        this.serverUrl = serverUrl;
        this.repl = repl;
    }

    @Override
    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String login(String[] params) throws ResponseException {
        throw new ResponseException(ResponseException.Code.ClientError,
                "login not implemented");
    }

    private String register(String[] params) throws ResponseException {
        throw new ResponseException(ResponseException.Code.ClientError,
                "register not implemented");
    }

    @Override
    public void quit() {}

    @Override
    public String help() {
        return "Commands: login, register, quit";
    }
}
