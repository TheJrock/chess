package ui;

public interface Client {
    void quit(Repl repl);
    void help();
    void eval(String input, Repl repl);
}
