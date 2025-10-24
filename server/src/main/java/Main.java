import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import server.Server;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        try {
            var port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            DataAccess dataAccess = new MemoryDataAccess();
            if (args.length >= 2 && args[1].equals("sql")) {
                dataAccess = new MemoryDataAccess();
            }

            var service = new UserService(dataAccess);
            var server = new Server();
            server.run(port);
            port = server.port();
            System.out.printf("Server started on port %d with %s%n", port, dataAccess.getClass());
            return;
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
        System.out.println("♕ 240 Chess Server");
    }
}