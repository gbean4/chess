package server;

import com.google.gson.Gson;
import dataaccess.MySqlDataAccess;
import datamodel.*;
import exception.DataAccessException;
import exception.ResponseException;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

import java.util.Map;

public class Server {
    private final Javalin server;
    private final UserService userService;

    public Server() {
        MySqlDataAccess dataAccess;
        try {
            dataAccess = new MySqlDataAccess();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", this::clear);
        server.post("user", this::register); //ctx.result("{ \"username\":\"\", \"authToken\":\"\" }")
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.get("game", this::listGames);
        server.get("game/{id}", this::getGame);
        server.post("game", this::createGame);
        server.put("game", this::joinGame);
        server.put("/game/leave", this::leaveGame);
        server.put("game/resign", this::resignGame);
    }

    private void handleException(Context ctx, Exception ex){
        var serializer= new Gson();
        int statusCode;
        var msg = (ex.getMessage() != null)? ex.getMessage().toLowerCase(): "";
        if (msg.contains("401") || msg.contains("unauthorized")) {
            statusCode = 401;
        } else if (msg.contains("400") || msg.contains("missing required") || msg.contains("bad request")){
            statusCode = 400;
        } else if (msg.contains("403") || msg.contains("forbidden") || msg.contains("already exists")|| msg.contains("already taken")){
            statusCode = 403;
        }else{
            statusCode = 500;
        }

        ctx.status(statusCode).result(serializer.toJson(Map.of(
                "message", "Error: " + ex.getMessage(),
                "status", statusCode)));
    }

    private void clear(Context ctx){
        var serializer = new Gson();
        try {
            MySqlDataAccess dataAccess = new MySqlDataAccess();
            dataAccess.clear();
            ctx.status(200).result(serializer.toJson(null));
        } catch (Exception ex) {
            ctx.status(500).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        }
    }

    private void register(Context ctx){
        var serializer = new Gson();
        try {
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);
            if (user.username()==null || user.password()==null ||user.email()==null){
                throw new Exception("Missing required fields");
            }

            var registrationResponse = userService.register(user);
            ctx.result(serializer.toJson(registrationResponse));

        } catch (Exception ex){
            int statusCode = getStatusCode(ex);

            ctx.status(statusCode).result(serializer.toJson(Map.of(
                    "message", "Error: " + ex.getMessage(),
                    "status", statusCode)));        }
    }

    private static int getStatusCode(Exception ex) {
        int statusCode;
        var msg = (ex.getMessage() != null)? ex.getMessage().toLowerCase(): "";
        if (msg.contains("401") || msg.contains("unauthorized")) {
            statusCode = 401;
        } else if (msg.contains("400") || msg.contains("missing required")){
            statusCode = 400;
        } else if (msg.contains("403") || msg.contains("forbidden")|| msg.contains("already exists")) {
            statusCode = 403;
        }else{
            statusCode = 500;
        }
        return statusCode;
    }

    private void login(Context ctx){
        var serializer = new Gson();
        try {
            var req = serializer.fromJson(ctx.body(), LoginRequest.class);
            var result = userService.login(req.username(), req.password());
            ctx.status(200).result(serializer.toJson(result));
        } catch (Exception ex){
            handleException(ctx, ex);
        }
    }

    private void logout(Context ctx){
        try {
            String authToken = ctx.header("authorization");

            userService.logout(authToken);
            ctx.status(200).result("{}");

        } catch (Exception ex){
            handleException(ctx, ex);
        }
    }

    private void createGame(Context ctx){
        var serializer = new Gson();
        try{
            var req = serializer.fromJson(ctx.body(), CreateGameRequest.class);
            if (req == null || req.gameName() == null || req.gameName().isEmpty()){
                throw new Exception("400: missing required fields");
            }
            String authToken = ctx.header("authorization");
//            GameData gameData = userService.createGame(authToken, req.gameName());
            int gameID = userService.createGame(authToken, req.gameName());


            ctx.status(200).result(serializer.toJson(Map.of("gameID", gameID)));
        } catch (Exception ex){
            handleException(ctx, ex);
        }
    }

    private void listGames(Context ctx){
        var serializer = new Gson();
        try {
            String authToken = ctx.header("authorization");
            var games = userService.listGames(authToken);
            ctx.status(200).result(serializer.toJson(Map.of("games", games)));

        } catch (Exception ex){
            handleException(ctx, ex);
        }
    }

    private void getGame(Context ctx){
        var serializer = new Gson();
        try {
            String authToken = ctx.header("authorization");
            int gameID = Integer.parseInt(ctx.pathParam("id"));
            var game = userService.getGame(authToken, gameID);
            ctx.status(200).result(serializer.toJson(game));

        } catch (Exception ex){
            handleException(ctx, ex);
        }
    }

    private void joinGame(Context ctx){
        var serializer = new Gson();
        try {
            String authToken = ctx.header("authorization");
            var gameSpec = serializer.fromJson(ctx.body(), GameSpec.class);

            userService.joinGame(authToken, gameSpec);
            ctx.status(200).result("{}");
        } catch (Exception ex){
            int statusCode;
            var msg = ex.getMessage().toLowerCase();

            if (msg.contains("401")) {
                statusCode = 401;
            } else if (msg.contains("400") || msg.contains("bad request")) {
                statusCode = 400;
            } else if (msg.contains("already taken")|| msg.contains("already exists")||(msg.contains("403"))){
                statusCode = 403;
            } else{
                statusCode = 500;
            }
            ctx.status(statusCode).result(serializer.toJson(Map.of(
                    "message", "Error: " + ex.getMessage(),
                    "status", statusCode)));
        }
    }

    private void leaveGame(Context ctx){
        var serializer = new Gson();
        try{
            String authToken = ctx.header("authorization");
            var request = serializer.fromJson(ctx.body(), GameSpec.class);
            userService.leaveGame(authToken, request);
            ctx.status(200).result("{}");
        } catch(Exception ex){
            handleException(ctx, ex);
        }
    }

    private void resignGame(Context ctx){
        var serializer = new Gson();
        try{
            String authToken = ctx.header("authorization");
            var request = serializer.fromJson(ctx.body(), GameSpec.class);
            userService.resignGame(authToken, request);
            ctx.status(200).result("{}");
        } catch(Exception ex){
            handleException(ctx, ex);
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
