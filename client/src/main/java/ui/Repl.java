package ui;

import java.util.Scanner;
import static java.awt.Color.*;

public class Repl {
    private Client client;
    public Repl(String serverUrl) {
        client = new LoggedOutClient(serverUrl, this);
    }

    public void run() {
        System.out.println("Welcome to Chess! Please login or register to play a game.");
        System.out.println(client.help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String input = scanner.nextLine();
            try {
                result = client.eval(input);
                System.out.print(BLUE + result);
            } catch (Throwable e) {
                String error = e.toString();
                System.out.println(error);
            }
        }
        System.out.println("Goodbye!");
    }

//    public void notify(Notification notification) {
//        System.out.println(RED + notification.message());
//        printPrompt();
//    }

    private void printPrompt() {
        System.out.print("\n" + ">>> " + GREEN);
    }
}
