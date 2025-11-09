package ui;


import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import exception.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

public class GameUI {
    private final ChessGame game;
    private final String playerColor;

    public GameUI(ChessGame game, String playerColor) {
        this.game = game;
        this.playerColor = playerColor;
    }

    public void render(){
        ChessGame.TeamColor perspective= null;

        if (playerColor != null){
           if (playerColor.equalsIgnoreCase("white")){
               perspective = ChessGame.TeamColor.WHITE;
           } else if (playerColor.equalsIgnoreCase("black")){
               perspective = ChessGame.TeamColor.BLACK;
            }
        }
        ChessBoardUI.renderBoard(game, perspective);
    }

    public String handleCommand(String input) throws ResponseException{
        String[] tokens = input.toLowerCase().split("\\s+");
        String cmd = (tokens.length > 0) ? tokens[0].toLowerCase(): "";

        switch(cmd){
            case "move" -> {
                if (tokens.length != 3) return "Usage: move <from> <to>";
                return game.makeMove(new ChessMove(tokens[1], tokens[2], game.getBoard().getPiece(new ChessPosition(tokens[1]))));
            }
            case "board" -> {
                render();
                return "";
            }
            case "resign" ->{
                resign();
                return "You resigned.";
            }
            case "leave"-> {
                leave();
                return "You left the game.";
            }
            default -> {
                return "Unknown command. Type help for available commands.";
            }
        }
    }

}
