package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import service.UserService;
import websocket.commands.*;
import websocket.messages.*;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();
    private final UserService service;


    public WebSocketHandler(UserService service){
        this.service = service;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        try {
            UserGameCommand cmd = gson.fromJson(ctx.message(), UserGameCommand.class);
            Session session = ctx.session;

            switch (cmd.getCommandType()) {
                case CONNECT -> onConnect(cmd, session);
                case MAKE_MOVE -> onMove((MakeMoveCommand) cmd, session);
                case LEAVE -> onLeave(cmd, session);
                case RESIGN -> onResign(cmd, session);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorMessage err = new ErrorMessage("Invalid command format");
            try{
                ctx.session.getRemote().sendString(gson.toJson(err));
            } catch(Exception ignore){}
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void onConnect(UserGameCommand cmd, Session session) throws Exception {
        String auth = cmd.getAuthToken();
        int gameID = cmd.getGameID();

        service.validate(auth);
        connections.add(gameID, session, auth);

        Object game = service.getGame(auth, gameID);
        LoadGameMessage msg = new LoadGameMessage(game);

        session.getRemote().sendString(gson.toJson(msg));

        NotificationMessage joined = new NotificationMessage(service.validate(auth).username() + " has joined.");
        connections.broadcast(gameID, session, gson.toJson(joined));
    }

    private void onMove(MakeMoveCommand cmd, Session session) throws Exception{
        String auth = cmd.getAuthToken();
        int gameID = cmd.getGameID();
        var move = cmd.getMove();

        service.validate(auth);

        try{
            ChessGame updated = service.applyMove(auth, gameID, move);
            LoadGameMessage msg = new LoadGameMessage(updated);
            String json = gson.toJson(msg);

            connections.broadcast(gameID, null, json);
        } catch (Exception e){
            ErrorMessage err = new ErrorMessage(e.getMessage());
            session.getRemote().sendString(gson.toJson(err));
        }
    }

    private void onLeave(UserGameCommand cmd, Session session) throws Exception{
        int gameID = cmd.getGameID();
        connections.remove(gameID,session);
        var user = service.validate(cmd.getAuthToken());
        NotificationMessage notify = new NotificationMessage(user.username() +" has left the game.");

        connections.broadcast(gameID, session, gson.toJson(notify));
    }

    private void onResign(UserGameCommand cmd, Session session) throws Exception{
        int gameID = cmd.getGameID();

        var user = service.validate(cmd.getAuthToken());
        NotificationMessage notify = new NotificationMessage(user.username() +" has resigned.");

        connections.broadcast(gameID, null, gson.toJson(notify));
        connections.remove(gameID,session);
    }
}
