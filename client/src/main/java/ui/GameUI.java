package ui;


import chess.*;
import exception.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

public class GameUI {
    private final ChessGame game;
    private final String playerColor;
    private final ServerFacade server;
    private final String authToken;
    private final int gameID;

    public GameUI(ChessGame game, String playerColor, ServerFacade server, String authToken, int gameID) {
        this.game = game;
        this.playerColor = playerColor;
        this.server = server;
        this.authToken = authToken;
        this.gameID =  gameID;
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

    public String leave() throws ResponseException{
        server.leaveGame(authToken, gameID);
        return "You left the game";
    }
    public String resign() throws ResponseException{
        server.resignGame(authToken, gameID);
        return "You resigned. Game over.";
    }

    public String handleCommand(String input) throws ResponseException, InvalidMoveException {
        String[] tokens = input.toLowerCase().split("\\s+");
        String cmd = (tokens.length > 0) ? tokens[0].toLowerCase(): "";
        ChessPosition from = posConvert(tokens[1]);
        ChessPosition to = posConvert(tokens[2]);

        return switch(cmd){
            case "move" -> {
                if (tokens.length != 3) yield "Usage: move <from> <to>";
                game.makeMove(new ChessMove(from, to, game.getBoard().getPiece(from).getPieceType()));
                yield "";
            }
            case "board" -> {
                render();
                yield "";
            }
            case "resign" -> resign();
            case "leave"-> leave();
            default -> {
                yield "Unknown command. Type help for available commands.";
            }
        };
    }
    public static ChessPosition posConvert(String pos){
        char file = pos.charAt(0);
        char rank = pos.charAt(1);

        int col = file - 'a';
        int row = 8- (rank - '0');
        return new ChessPosition(row,col);
    }

    public int getGameID(){
        return gameID;
    }
}
