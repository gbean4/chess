package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx->ctx.result("{}"));
        server.post("user", this::register); //ctx.result("{ \"username\":\"\", \"authToken\":\"\" }")
    }

    private void clear(Context ctx){
        ctx.result("{}");
    }

    private void register(Context ctx){

        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);
            var registrationResponse = userService.register(user);
            ctx.result(serializer.toJson(registrationResponse));
        } catch (Exception ex){
            ctx.status(403).result(ex.getMessage()); //maybe put in error message
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
