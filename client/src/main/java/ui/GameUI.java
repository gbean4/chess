package ui;


import chess.ChessBoard;
import chess.ChessGame;
import server.ServerFacade;

public class GameUI {
    private final ChessGame game;
    private final ChessBoard board;
    private final String playerColor;

    public GameUI(ChessGame game, String playerColor, ServerFacade server) {
        this.game = game;
        this.board = game.getBoard();
        this.playerColor = playerColor;
    }

    public void render(){
        var renderer = new ChessBoardUI();
    }
}
