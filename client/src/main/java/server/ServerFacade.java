package server;

import com.google.gson.Gson;
import datamodel.*;
import exception.ResponseException;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }
        this.serverUrl = serverUrl;
    }

    public RegisterResponse register(RegisterRequest request) throws ResponseException {
        var httpRequest = buildRequest("POST", "user", request);
        var response = sendRequest(httpRequest);
        return handleResponse(response, RegisterResponse.class);
    }

    public AuthData login(LoginRequest request) throws ResponseException {
        var httpRequest = buildRequest("POST", "session", request);
        var response = sendRequest(httpRequest);
        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var httpRequest = buildRequest("DELETE", "session", null, authToken);
        var response = sendRequest(httpRequest);
        handleResponse(response, null);
    }

    public void clear() throws ResponseException {
        var httpRequest = buildRequest("DELETE", "db", null);
        sendRequest(httpRequest);
    }

    public void createGame(CreateGameRequest request, String authToken) throws ResponseException {
        var httpRequest = buildRequest("POST", "game", request, authToken);
        var response = sendRequest(httpRequest);
        handleResponse(response, CreateGameResult.class);
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        var httpRequest = buildRequest("GET", "game", null, authToken);
        var response = sendRequest(httpRequest);
        return handleResponse(response, ListGamesResponse.class);
    }

    public GameData joinGame(GameSpec gameSpec, String authToken) throws ResponseException {
        var path = "game";
        var httpRequest = buildRequest("PUT", path, gameSpec, authToken);
        var response = sendRequest(httpRequest);
        return handleResponse(response, GameData.class);
    }

    public GameData getGame(String authToken, int gameID) throws ResponseException{
        var httpRequest = buildRequest("GET", "game/" + gameID, null, authToken);
        var response = sendRequest(httpRequest);
        return handleResponse(response, GameData.class);
    }

    public void leaveGame(String authToken, int gameID) throws ResponseException{
        var path = "game/"+gameID+"/leave";
        var req = new LeaveResignRequest(authToken, gameID);
        var httpRequest = buildRequest("PUT", path, req, authToken);
        var response = sendRequest(httpRequest);
        handleResponse(response, null);
    }

    public void resignGame(String authToken, int gameID) throws ResponseException{
        var path = "game/"+gameID+"/resign";
        var req = new LeaveResignRequest(authToken, gameID);
        var httpRequest = buildRequest("PUT", path, req, authToken);
        var response = sendRequest(httpRequest);
        handleResponse(response, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body){
        return buildRequest(method, path, body, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .header("Accept", "application/json");
        if (authToken != null){
            builder.header("authorization", authToken);
        }
        if (body != null) {
            String json = gson.toJson(body);
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(json));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        String body = response.body();
        if (status == 200) {
            if (responseClass == null ||body == null|| body.isEmpty()) {
                return null;
            }
            return gson.fromJson(body,responseClass);
        }

        throw ResponseException.fromJson(body);
    }
}