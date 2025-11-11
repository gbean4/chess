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
import java.util.Map;

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

//    public void clear() throws ResponseException {
//        var httpRequest = buildRequest("DELETE", "db", null);
//        sendRequest(httpRequest);
//    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ResponseException {
        var httpRequest = buildRequest("POST", "game", request, authToken);
        var response = sendRequest(httpRequest);
        return handleResponse(response, CreateGameResult.class);
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
        String body = response.body();
        if (status == 200) {
            if (responseClass == null ||body == null|| body.isEmpty()) {
                return null;
            }
            return gson.fromJson(body,responseClass);
        }
        try{
            throw ResponseException.fromJson(body);
        } catch (Exception e){
            throw new ResponseException(ResponseException.fromHttpStatusCode(status), body);
        }

//        if (responseClass != null) {
//            T result = new Gson().fromJson(response.body(), responseClass);
//            if (result == null){
//                throw new ResponseException(ResponseException.Code.ServerError,
//                        "Server returned empty or invalid response: " + response.body());
//            }
//            return new Gson().fromJson(response.body(), responseClass);
//        }
//
//        return null;
    }
}