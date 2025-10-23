package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.UserAlreadyExistsException;
import io.javalin.*;
import io.javalin.http.*;
import com.google.gson.Gson;
//import javax.naming.Context;
import java.util.Map;
import datamodel.*;
import io.javalin.http.staticfiles.Location;
import org.w3c.dom.CDATASection;
import service.UserService;
import io.javalin.json.JavalinJackson;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server {

    private final Javalin server;
    private final DataAccess dataAccess;
    private final UserService userService;
    private final Gson gson = new Gson();

    public Server() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);

        server = Javalin.create(config -> config.staticFiles.add("/web", Location.CLASSPATH));

        server.delete("/db", this::clearDatabase);
        server.post("/user", this::register);
    }

    private void clearDatabase(Context ctx) {
        try {
            dataAccess.clear();
            ctx.status(200).result("{}").contentType("application/json");
        } catch (Exception ex) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + ex.getMessage())));
        }
    }

    private void register(Context ctx) {
//        try {
//            var serializer = new Gson();
//            String reqJson = ctx.body();
//            var user = serializer.fromJson(reqJson, UserData.class);
//
//            var authData = userService.register(user);
//
//            ctx.result(serializer.toJson(authData));
//        } catch (Exception ex) {
//            var message = String.format("Error: %s", ex.getMessage());
//            ctx.status(403).result(message);
//        }
        try {
            UserData user = gson.fromJson(ctx.body(), UserData.class);
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
