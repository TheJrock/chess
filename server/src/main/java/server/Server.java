package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.UserAlreadyExistsException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.UserService;
import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import datamodel.AuthData;
import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService service;
    private final Gson serializer;

    public Server() {
        service = new UserService(new MemoryDataAccess());
        serializer = new Gson();

        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.post("/user", this::register);
        server.post("/session", this::login);
        server.delete("/session", this::logout);
        server.delete("/db", this::clearDatabase);
        server.exception(DataAccessException.class, (ex, ctx) -> ctx.status(500).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage()))));
    }

    private void clearDatabase(Context ctx) {
        try {
            service.clear();
            ctx.status(200).result("{}").contentType("application/json");
        } catch (Exception ex) {
            ctx.status(500).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        }
    }

    private void register(Context ctx) {
        try {
            String reqJson = ctx.body();
            UserData user = serializer.fromJson(reqJson, UserData.class);
            AuthData authData = service.register(user);
            ctx.contentType("application/json");
            ctx.status(200).result(serializer.toJson(authData));
        } catch (IllegalArgumentException ex) {
            ctx.status(400).json(Map.of("message", "Error: " + ex.getMessage()));
        } catch (UserAlreadyExistsException ex) {
            ctx.status(403).json(Map.of("message", "Error: already taken"));
        } catch (Exception ex) {
            ctx.status(500).json(Map.of("message", "Error: " + ex.getMessage()));
        }
    }

    private void login(Context ctx) {
        try {
            String reqJson = ctx.body();
            UserData user = serializer.fromJson(reqJson, UserData.class);
            AuthData authData = service.login(user.username(), user.password());
            ctx.contentType("application/json");
            ctx.status(200).result(serializer.toJson(authData));
        } catch (IllegalArgumentException ex) {
            ctx.status(400).json(Map.of("message", "Error: " + ex.getMessage()));
        } catch (Exception ex) {
            ctx.status(500).json(Map.of("message", "Error: " + ex.getMessage()));
        }
    }

    private void logout(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            service.logout(authToken);
            ctx.status(200)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Logout successful")));
        } catch (UnauthorizedException ex) {
            ctx.status(401)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        } catch (Exception ex) {
            ctx.status(500)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    public int port() {
        return server.port();
    }
}