package server;

import com.google.gson.Gson;
import datamodel.*;
import exception.ResponseException;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson gson =  new Gson();

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData register(UserData userData) throws ResponseException {
        var request = buildRequest("POST", "/user", userData, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException {
        var loginRequest = new LoginRequest(username, password);
        var request = buildRequest("POST", "/session", loginRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var request = buildRequest("DELETE", "/session", null, authToken);
        var response = sendRequest(request);
        handleResponse(response, Void.class);
    }

    public int create(String authToken, String gameName) throws ResponseException {
        var listRequest = new CreateGameRequest(gameName);
        var request = buildRequest("POST", "/game", listRequest, authToken);
        var response = sendRequest(request);
        return handleResponse(response, CreateGameResponse.class).gameID();
    }

    public GameData[] list(String authToken) throws ResponseException {
        var request = buildRequest("GET", "/game", null, authToken);
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResponse.class).games();
    }

    public void join(String authToken, String team, int gameID) throws ResponseException {
        var joinRequest = new JoinRequest(team, gameID);
        var request = buildRequest("PUT", "/join", joinRequest, authToken);
        var response = sendRequest(request);
        handleResponse(response, Void.class);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path));
        if (authToken != null) {
            builder.header("Authorization", authToken);
        }
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        builder.method(method, makeRequestBody(body));
        return builder.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(gson.toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        int status = response.statusCode();
        String body = response.body();
        if (!isSuccessful(status)) {
            try {
                throw ResponseException.fromJson(body);
            } catch (Exception e) {
                throw new ResponseException(ResponseException.fromHttpStatusCode(status),
                        "Server error (" + status + "): " + body);
            }
        }
        if (responseClass == Void.class) {
            return null;
        }
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return gson.fromJson(body, responseClass);
        } catch (Exception e) {
            var code = ResponseException.fromHttpStatusCode(status);
            throw new ResponseException(code, "Invalid JSON in response");
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
