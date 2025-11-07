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

    public ServerFacade(String url) {
        serverUrl = url.endsWith("/")? url : url + "/";
    }

    public RegisterResponse register(RegisterResponse req) throws ResponseException {
        var request = buildRequest("POST", "user",req, null);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResponse.class);
    }

    public void login(LoginRequest loginReq) throws ResponseException {
        var path = String.format("/session/%s", loginReq);
        var request = buildRequest("POST", path, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void logout(String authToken) throws ResponseException {
        var path = String.format("/session/%s", authToken);
        var request = buildRequest("DELETE", path, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void clear() throws ResponseException {
        var request = buildRequest("DELETE", "db", null);
        sendRequest(request);
    }

    public CreateGameRequest createGame(CreateGameRequest gameResponse) throws ResponseException {
        var request = buildRequest("POST", "game", this::register);
        var response = sendRequest(request);
        return handleResponse(response, gameID);
    }

    public GameData[] listGames() throws ResponseException {
        var request = buildRequest("GET", "game", null);
        var response = sendRequest(request);
        return handleResponse(response, games);
    }

    public void joinGame(GameSpec gameSpec) throws ResponseException {
        var path = String.format("/game/%s", gameSpec);
        var request = buildRequest("PUT", path, GameSpec.class);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

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