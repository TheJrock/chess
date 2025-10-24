package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.UserAlreadyExistsException;
import datamodel.GameData;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.UserService;
import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import datamodel.AuthData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService service;
    private static final Gson serializer = new Gson();

    public Server() {
        service = new UserService(new MemoryDataAccess());

        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.post("/user", this::register);
        server.post("/session", this::login);
        server.delete("/session", this::logout);
        server.post("/game", this::create);
        server.get("/game", this::list);
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
            ctx.status(200)
                    .contentType("application/json")
                    .result(serializer.toJson(authData));
        } catch (IllegalArgumentException ex) {
            ctx.status(400)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        } catch (UserAlreadyExistsException ex) {
            ctx.status(403)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        } catch (Exception ex) {
            ctx.status(500)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        }
    }

    private void login(Context ctx) {
        try {
            String reqJson = ctx.body();
            UserData user = serializer.fromJson(reqJson, UserData.class);
            AuthData authData = service.login(user.username(), user.password());
            ctx.status(200)
                    .contentType("application/json")
                    .result(serializer.toJson(authData));
        } catch (IllegalArgumentException ex) {
            ctx.status(400)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        } catch (Exception ex) {
            ctx.status(500)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
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

    private void create(Context ctx) {
        try {
            String reqJson = ctx.body();
            GameData gameData = serializer.fromJson(reqJson, GameData.class);
            String authToken = ctx.header("Authorization");
            String gameId = service.create(authToken, gameData.gameName());
            ctx.status(200)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("gameID", gameId)));
        } catch (IllegalArgumentException ex) {
            ctx.status(400)
                    .contentType("application/json")
                    .result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
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

    private void list(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            var games = service.list(authToken);
            var gameList = new ArrayList<>(games.values());
            var response = Map.of("games", gameList);
            ctx.status(200)
                    .contentType("application/json")
                    .result(serializer.toJson(response));
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