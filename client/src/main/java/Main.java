import chess.*;
import client.ChessClient;
import ui.GameUI;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        ChessClient client = new ChessClient(serverUrl);
        GameUI gameUI = new GameUI(client);

        client.setGameUI(gameUI);

        client.run();
    }
}