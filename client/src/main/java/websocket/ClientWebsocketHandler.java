package websocket;

import client.ChessClient;
import com.google.gson.Gson;
//import jakarta.websocket.MessageHandler;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import static websocket.messages.ServerMessage.ServerMessageType.*;

public class ClientWebsocketHandler{
    private final ClientNotificationHandler notificationHandler;
    private final Gson gson = new Gson();

    public ClientWebsocketHandler(ChessClient client) {
        this.notificationHandler = new ClientNotificationHandler(client);
    }

//    @Override
    public void onMessage(String json) {
        ServerMessage envelope = gson.fromJson(json, ServerMessage.class);

        switch (envelope.getServerMessageType()){
            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                notificationHandler.loadGame(msg);
            }
            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                notificationHandler.error(msg);
            }
            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);
                notificationHandler.notify(msg);
            }
        }
    }
}
