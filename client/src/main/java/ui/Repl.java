package ui;

import server.ServerFacade;
import java.util.Scanner;

public class Repl {

    private Client client;

    public Repl(String serverUrl) {
        ServerFacade facade = new ServerFacade(serverUrl);
        this.client = new LoggedOutClient(facade);
    }

    public void run() {
        System.out.println("Welcome to Chess! Please login or register to play a game.");
        client.help();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            printPrompt();
            String input = scanner.nextLine();

            try {
                client.eval(input, this);
            } catch (Throwable e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printPrompt() {
        System.out.print("\n>>> ");
    }

    public void setClient(Client newClient) {
        this.client = newClient;
    }
}
