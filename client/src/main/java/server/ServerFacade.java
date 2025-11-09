package server;

import com.google.gson.Gson;
import datamodel.*;
import exception.ResponseException;
//import model.*;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
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
//        this.client = HttpClient.newHttpClient();
    }

    public RegisterResponse register(RegisterResponse request) throws ResponseException {
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

    public int createGame(CreateGameRequest request, String authToken) throws ResponseException {
        var httpRequest = buildRequest("POST", "game", request, authToken);
        var response = sendRequest(httpRequest);
        return handleResponse(response, Integer.class);
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        var httpRequest = buildRequest("GET", "game", null, authToken);
        var response = sendRequest(httpRequest);
        return handleResponse(response, ListGamesResponse.class);
    }

    public GameData joinGame(GameSpec gameSpec) throws ResponseException {
        var path = String.format("/game/%s", gameSpec);
        var httpRequest = buildRequest("PUT", path, GameSpec.class);
        var response = sendRequest(httpRequest);
        return handleResponse(response, GameData.class);
    }

    public void leaveGame(String authToken, int gameID) throws ResponseException{
        var req = new LeaveResignRequest(authToken, gameID);
        var httpRequest = buildRequest("PUT", "/game/leave", req);
        var response = sendRequest(httpRequest);
        handleResponse(response, null);
    }

    public void resignGame(String authToken, int gameID) throws ResponseException{
        var req = new LeaveResignRequest(authToken, gameID);
        var httpRequest = buildRequest("PUT", "/game/resign", req);
        var response = sendRequest(httpRequest);
        handleResponse(response, null);
    }
//    public ChessGame getGame(int gameID, authToken){
//        return
//    }

    private HttpRequest buildRequest(String method, String path, Object body){
        return buildRequest(method, path, body, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .header("Accept", "application/json");
        //        var request = HttpRequest.newBuilder()
//                .uri(URI.create(serverUrl + path))
//                .method(method, makeRequestBody(body));
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
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}