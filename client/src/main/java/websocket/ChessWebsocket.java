package websocket;

import client.ChessClient;
import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.net.URI;
@ClientEndpoint
public class ChessWebsocket {
    private Session session;
    private final Gson gson = new Gson();
    private final ClientNotificationHandler handler;
    private final String authToken;
    private final int gameID;

    public ChessWebsocket(String url, String authToken, int gameID, ChessClient client) throws ResponseException {
        this.handler = new ClientNotificationHandler(client);
        this.authToken = authToken;
        this.gameID = gameID;
        try {
            url = url.replaceFirst("^http","ws").replaceAll("/+$", "");
            URI uri = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, uri);

        } catch (Exception e) {
            System.out.println("WS CONNECT ERROR: "+ e);
            throw new ResponseException(e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) throws ResponseException {
        this.session = session;
        System.out.println("WebSocket connected! on open");
//        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT,authToken, gameID));


        try{
            sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT,authToken, gameID));
            System.out.println("connection sent");
        } catch (Exception e) {
            System.out.println("Failed sending CONNECT: "+ e.getMessage());
        }
    }

    @OnMessage
    public void onMessage(String json){
        handleIncomingMessage(json);
    }

    @OnClose
    public void close(){
        try{
            if (session != null && session.isOpen()){
                session.close();
            }
        }catch (Exception ignored){}
    }

    private void handleIncomingMessage(String json){
        ServerMessage base = gson.fromJson(json, ServerMessage.class);

        switch(base.getServerMessageType()){
            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                handler.loadGame(msg);
            }
            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                handler.error(msg);
            }
            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);
                handler.notify(msg);
            }
        }
    }

    public void sendCommand(UserGameCommand cmd) throws ResponseException{
        try{
            String json = gson.toJson(cmd);
            session.getBasicRemote().sendText(json);
        } catch(Exception e){
            throw new ResponseException(e.getMessage());
        }
    }

}
