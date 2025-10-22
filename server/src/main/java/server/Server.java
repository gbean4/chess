package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import datamodel.GameData;
import datamodel.GameSpec;
import datamodel.LoginRequest;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

import java.util.Arrays;
import java.util.Map;

public class Server {
//    private static final MemoryDataAccess dataAccess = new MemoryDataAccess();
    private final Javalin server;
    private final UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", this::clear);
        server.post("user", this::register); //ctx.result("{ \"username\":\"\", \"authToken\":\"\" }")
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.get("game", this::listGames);
        server.post("game", this::createGame);
        server.put("game", this::joinGame);
    }


    private void clear(Context ctx){
        var dataAccess = new MemoryDataAccess();
        dataAccess.clear();
        ctx.result("{}");
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
            String message = ex.getMessage() == null? "" : ex.getMessage().toLowerCase();
            int statusCode = 400;
//            int statusCode = ex.getMessage().toLowerCase().contains("exists")? 403:400;
            if (message.contains("exists") || message.contains("forbidden") || message.contains("unauthorized")){
                statusCode = 403;
            }
            ctx.status(statusCode).result(serializer.toJson(Map.of("message", "Error: "+ ex.getMessage())));

        }
    }
    private void login(Context ctx){
        var serializer = new Gson();
        try {
            var req = serializer.fromJson(ctx.body(), LoginRequest.class);
            var result = userService.login(req.username(), req.password());
//            ctx.result(serializer.toJson(result));
            ctx.status(200).result(serializer.toJson(result));
        } catch (Exception ex){
            int statusCode = ex.getMessage().toLowerCase().contains("400")? 400:401;
            ctx.status(statusCode).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        }
    }

    private void logout(Context ctx){
        var serializer = new Gson();
        try {
//            var req = serializer.fromJson(ctx.body(), AuthData.class);
            String authToken = ctx.header("authorization");

            userService.logout(authToken);
            ctx.status(200).result("{}");

        } catch (Exception ex){
            ctx.status(401).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        }
    }

    private void createGame(Context ctx){
        var serializer = new Gson();
        try{
            var name = serializer.fromJson(ctx.body(), String.class);
            if (name == null || name.isEmpty()){
                throw new Exception("400: missing required fields");
            }
            String authToken = ctx.header("authorization");

            GameData gameData = userService.createGame(authToken, name);
            ctx.status(200).result(String.valueOf(gameData.gameID()));
        } catch (Exception ex){
            int statusCode = 400;
            var msg = ex.getMessage().toLowerCase();
//            if (msg.contains("400")) statusCode = 400;
            if (msg.contains("401")) {
                statusCode = 401;
            }

            ctx.status(statusCode).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
        }
    }

    private void listGames(Context ctx){
        var serializer = new Gson();
        try {
            String authToken = ctx.header("authorization");
            var games = userService.listGames(authToken);
            ctx.status(200).result(Arrays.toString(games));
        } catch (Exception ex){
            ctx.status(401).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
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
            int statusCode = 400;
            var msg = ex.getMessage().toLowerCase();

            if (msg.contains("401")) {
                statusCode = 401;
            }
            if (msg.contains("already taken")) {
                statusCode = 403;
            }
            ctx.status(statusCode).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
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
