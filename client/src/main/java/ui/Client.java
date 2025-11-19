package ui;

public interface Client {
    enum State {
        LOGGED_OUT,
        LOGGED_IN,
        IN_GAME
    }
    void quit();
    String help();
    String eval(String input);
}
