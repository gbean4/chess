package websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.net.URI;

public class ChessWebsocket extends Endpoint {
    private Session session;
    private final NotificationHandler handler;
    private final Gson gson = new Gson();
    private final String authToken;

    public ChessWebsocket(String url, String authToken, NotificationHandler handler) throws ResponseException {
        try {
            this.handler = handler;
            this.authToken = authToken;

            url = url.replace("http", "ws");
            URI uri = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, uri);

            this.session.addMessageHandler((MessageHandler.Whole<String>) this::handleIncomingMessage);

        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
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
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch(Exception e){
            throw new ResponseException(e.getMessage());
        }
    }

}
