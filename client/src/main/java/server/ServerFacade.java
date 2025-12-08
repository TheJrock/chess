package server;

import com.google.gson.Gson;
import datamodel.*;
import exception.ResponseException;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.util.HashMap;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public Pet addPet(Pet pet) throws ResponseException {
        var request = buildRequest("POST", "/pet", pet);
        var response = sendRequest(request);
        return handleResponse(response, Pet.class);
    }

    public void deletePet(int id) throws ResponseException {
        var path = String.format("/pet/%s", id);
        var request = buildRequest("DELETE", path, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void deleteAllPets() throws ResponseException {
        var request = buildRequest("DELETE", "/pet", null);
        sendRequest(request);
    }

    public PetList listPets() throws ResponseException {
        var request = buildRequest("GET", "/pet", null);
        var response = sendRequest(request);
        return handleResponse(response, PetList.class);
    }

    public AuthData register(UserData userData) throws ResponseException {
        var request = buildRequest("POST", "/user", userData);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException {
        var loginRequest = new LoginRequest(username, password);
        var request = buildRequest("POST", "/session", loginRequest);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var request = buildRequest("DELETE", "/session", authToken);
        sendRequest(request);
    }

    public int create(String authToken, String gameName) throws ResponseException {
        var request = buildRequest("POST", "/game", gameName);
        var response = sendRequest(request);
        return handleResponse(response, Integer.class);
    }

    public HashMap<Integer, GameData> list(String authToken) throws ResponseException {}

    public void join(String authToken, String team, int gameID) throws ResponseException {}

    private HttpRequest buildRequest(String method, String path, Object body) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path));
        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, makeRequestBody(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        int status = response.statusCode();
        String body = response.body();
        if (!isSuccessful(status)) {
            if (body != null && !body.isBlank()) {
                try {
                    throw ResponseException.fromJson(body);
                } catch (Exception _) {
                }
            }
            throw new ResponseException(
                    ResponseException.fromHttpStatusCode(status),
                    "Server error (" + status + "): " + body
            );
        }
        if (responseClass == null || responseClass == Void.class) {
            return null;
        }
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return new Gson().fromJson(body, responseClass);
        } catch (Exception e) {
            var code = ResponseException.fromHttpStatusCode(status);
            throw new ResponseException(code, "Invalid JSON in response");
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
