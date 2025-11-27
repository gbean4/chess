package websocket;

import client.ChessClient;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class ClientNotificationHandler implements NotificationHandler{

    private final ChessClient client;

    public ClientNotificationHandler(ChessClient client) {
        this.client = client;
    }

    @Override
    public void loadGame(LoadGameMessage message) {
        client.game = message.getGame();
    }

    @Override
    public void notify(NotificationMessage message) {

    }

    @Override
    public void error(ErrorMessage message) {

    }
}
