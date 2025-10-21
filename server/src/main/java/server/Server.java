package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.AuthData;
import datamodel.LoginRequest;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.ForbiddenException;
import service.UserService;

import java.util.Map;

public class Server {
    private static final MemoryDataAccess dataAccess = new MemoryDataAccess();
    private final Javalin server;
    private final UserService userService;

    public Server() {
//        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx->ctx.result("{}"));
        server.post("user", this::register); //ctx.result("{ \"username\":\"\", \"authToken\":\"\" }")
        server.post("session", this::login);
        server.delete("/session", this::logout);
    }

    private void clear(Context ctx){
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
            var req = serializer.fromJson(ctx.body(), AuthData.class);
//            String authHeader = ctx.header("authorization");

            userService.logout(req.authToken());
            ctx.status(200).result("{}");

        } catch (Exception ex){
            ctx.status(401).result(serializer.toJson(Map.of("message", "Error: " + ex.getMessage())));
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
