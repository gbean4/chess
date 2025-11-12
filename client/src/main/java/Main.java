import chess.*;
import client.ChessClient;
import server.ServerFacade;
import ui.GameUI;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        ServerFacade serverFacade = new ServerFacade(serverUrl);
        ChessClient client = new ChessClient(serverFacade);
        GameUI gameUI = new GameUI(client);

        client.setGameUI(gameUI);

        client.run();
    }
}