package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.UserAlreadyExistsException;
import io.javalin.*;
import io.javalin.http.*;
import java.util.Map;
import datamodel.*;
import io.javalin.http.staticfiles.Location;
import service.UserService;

public class Server {

    private final Javalin server;
    private final DataAccess dataAccess;
    private final UserService userService;
    private final ObjectMapper objectMapper;


    public Server() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        objectMapper = new ObjectMapper();

        server = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(objectMapper));
            config.staticFiles.add("/web", Location.CLASSPATH);
        });

        server.delete("/db", this::clearDatabase);
        server.post("/user", this::register);
    }

    private void clearDatabase(Context ctx) {
        try {
            dataAccess.clear();
            ctx.status(200).result(objectMapper.writeValueAsString(Map.of()));
        } catch (Exception ex) {
            try {
                String json = objectMapper.writeValueAsString(
                        Map.of("message", "Error: " + ex.getMessage())
                );
                ctx.status(500).result(json);
            } catch (Exception jsonEx) {
                ctx.status(500).result("{\"message\":\"Unknown error\"}");
            }
        }
    }

    private void register(Context ctx) {
        try {
            UserData user = ctx.bodyAsClass(UserData.class);
            AuthData authData = userService.register(user);

            ctx.status(200).json(authData);

        } catch (IllegalArgumentException ex) {
            ctx.status(400).json(Map.of("message", "Error: " + ex.getMessage()));
        } catch (UserAlreadyExistsException ex) {
            ctx.status(403).json(Map.of("message", "Error: already taken"));
        } catch (DataAccessException ex) {
            ctx.status(500).json(Map.of("message", "Error: " + ex.getMessage()));
        } catch (Exception ex) {
            ctx.status(500).json(Map.of("message", "Error: " + ex.getMessage()));
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
