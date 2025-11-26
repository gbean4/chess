package ui;


import chess.*;
import client.ChessClient;
import exception.ResponseException;
import server.ServerFacade;

import java.util.Arrays;


public class GameUI {
    private final ChessClient client;

    public GameUI(ChessClient client) {
        this.client = client;
    }

    public void render(){
        ChessGame.TeamColor perspective= null;
        String playerColor = client.getPlayerColor();
        ChessGame game = client.getGame();

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
        ServerFacade server = client.getServer();
        String authToken = client.getAuthToken();
        int gameID = client.getGameID();
        server.leaveGame(authToken, gameID);
        return "You left the game.";
    }
    public String resign() throws ResponseException{
        ServerFacade server = client.getServer();
        String authToken = client.getAuthToken();
        int gameID = client.getGameID();
        server.resignGame(authToken, gameID);
        return "You resigned. Game over.";
    }

    public String move(String... params) throws InvalidMoveException {
        if (params.length != 2) {
            return "Usage: move <from> <to>";
        }
        ChessPosition from = posConvert(params[0]);
        ChessPosition to = posConvert(params[1]);
        var game = client.getGame();

        if (game == null) {
            return "No game loaded. Try join <id> <color> first.";
        }
        game.makeMove(new ChessMove(from, to, game.getBoard().getPiece(from).getPieceType()));
        render();
        return "Move has been made";
    }


    public String handleCommand(String input) throws ResponseException, InvalidMoveException {
        String[] tokens = input.toLowerCase().split("\\s+");
        String cmd = (tokens.length > 0) ? tokens[0].toLowerCase(): "";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch(cmd){
            case "move" -> move(params);
            case "board" -> {
                render();
                yield "";
            }
            case "logout" -> client.logout();
            case "resign" -> resign();
            case "leave"-> leave();
            case "help"-> client.help();
            case "highlight moves"-> "input piece and get moves";
            case "back"-> "Returning to homescreen.";
            default -> "Unknown command. Type help for available commands.";
        };
    }

    public static ChessPosition posConvert(String pos){
        char file = pos.charAt(0);
        char rank = pos.charAt(1);

        int col = file - 'a';
        int row = 8- (rank - '0');
        return new ChessPosition(row,col);
    }
}
