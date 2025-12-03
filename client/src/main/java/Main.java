import chess.*;
import client.ChessClient;
import exception.ResponseException;
import server.ServerFacade;
import ui.GameUI;
import websocket.ChessWebsocket;

public class Main {
    public static void main(String[] args) throws ResponseException {
        String serverUrl = "http://localhost:8080";
        ServerFacade serverFacade = new ServerFacade(serverUrl);
        ChessClient client = new ChessClient(serverUrl);
        GameUI gameUI = new GameUI(client);

        client.setGameUI(gameUI);

        client.run();
    }
}