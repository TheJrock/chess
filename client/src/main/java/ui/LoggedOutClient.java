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
        if (params.length >= 1) {

            state = State.LOGGED_IN;
            visitorName = String.join("-", params);
            ws.enterPetShop(visitorName);
            return String.format("You signed in as %s.", visitorName);
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <yourname>");
    }

    private String register(String[] params) {
    }

    @Override
    public void quit() {

    }

    @Override
    public String help() {
        return "";
    }
}
