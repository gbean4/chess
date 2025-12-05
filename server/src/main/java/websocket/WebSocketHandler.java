package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.LeaveResignRequest;
import exception.ResponseException;
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
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCmd = gson.fromJson(ctx.message(), MakeMoveCommand.class);
                    onMove(moveCmd, session);
                }
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

        ChessGame game = service.getGame(auth, gameID).game();
        LoadGameMessage msg = new LoadGameMessage(game);

        session.getRemote().sendString(gson.toJson(msg));

        NotificationMessage joined = new NotificationMessage(service.validate(auth).username() + " has joined.\n");
        connections.broadcast(gameID, session, gson.toJson(joined));
    }

    private void onMove(MakeMoveCommand cmd, Session session) throws Exception{
        String auth = cmd.getAuthToken();
        int gameID = cmd.getGameID();
        var move = cmd.getMove();
        var start = move.getStartPosition();
        var end = move.getEndPosition();
        var startFile = convertColumn(start.getColumn());
        var endFile = convertColumn(end.getColumn());
        var startSquare = String.valueOf(startFile)+start.getRow();
        var endSquare = String.valueOf(endFile)+end.getRow();

        service.validate(auth);

        try{
            ChessGame updated = service.applyMove(auth, gameID, move);

            var username = service.validate(auth).username();
            var notifyText = username + " moved from " + startSquare + " to " + endSquare + "\n";
            var promotion = move.getPromotionPiece();
            if (promotion !=null){
                notifyText+= " (promoted to "+promotion+")\n";
            }
            var notifyMsg = new NotificationMessage(notifyText);
            var notifyJson = gson.toJson(notifyMsg);

            connections.broadcast(gameID,session,notifyJson);

            LoadGameMessage msg = new LoadGameMessage(updated);
            String json = gson.toJson(msg);

            connections.broadcast(gameID, null, json);
        } catch (Exception e){
            ErrorMessage err = new ErrorMessage(e.getMessage());
            session.getRemote().sendString(gson.toJson(err));
        }
    }

    private void onLeave(UserGameCommand cmd, Session session) throws Exception{
        try{
            int gameID = cmd.getGameID();
            var user = service.validate(cmd.getAuthToken());

            var req = new LeaveResignRequest(user.authToken(), gameID);
            service.leaveGame(req);
            NotificationMessage notify = new NotificationMessage(user.username() + " has left the game.\n");

            connections.broadcast(gameID, session, gson.toJson(notify));
            connections.remove(gameID, session);
        } catch (Exception e){
            if (e.getMessage().equals("You are not a player in this game.\n")){
                connections.remove(cmd.getGameID(), session);
                return;
            }
            ErrorMessage err = new ErrorMessage(e.getMessage());
            session.getRemote().sendString(gson.toJson(err));
        }
    }

    private void onResign(UserGameCommand cmd, Session session) throws Exception{
        try{
            int gameID = cmd.getGameID();

            var user = service.validate(cmd.getAuthToken());
            var req = new LeaveResignRequest(user.authToken(), gameID);
            service.resignGame(req);

            NotificationMessage notify = new NotificationMessage(user.username() + " has resigned.\n");

            connections.broadcast(gameID, null, gson.toJson(notify));

            connections.remove(gameID, session);
        } catch (Exception e){
            ErrorMessage err = new ErrorMessage(e.getMessage());
            session.getRemote().sendString(gson.toJson(err));
        }
    }

    private char convertColumn(int col){
        return (char)('a' + (col-1));
    }
}
