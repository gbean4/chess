package websocket;

import chess.ChessGame;
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
        ChessGame newGame = message.getGame();
        client.updateGame(newGame);
    }

    @Override
    public void notify(NotificationMessage message) {
        var text = "\n"+message.getMessage()+ "\n";
        client.displayNotification(text);
    }

    @Override
    public void error(ErrorMessage message) {
        String text = message.getErrorMessage();
        client.displayError(text);
    }
}
