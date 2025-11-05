import dataaccess.*;
import server.Server;

public class Main {

    public static void main(String[] args) {

        try {
            var port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            DataAccess dataAccess = new MemoryDataAccess();
            if (args.length >= 2 && args[1].equals("sql")) {
                dataAccess = new MysqlDataAccess();
            }

            var server = new Server(dataAccess);
            int run = server.run(port);
            System.out.printf("Server started on port %d with %s%n", run, dataAccess.getClass());

            return;

        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }

        System.out.println("♕ 240 Chess Server");
    }
}