package ui;


import chess.*;
import client.ChessClient;
import exception.ResponseException;
import server.ServerFacade;


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
        return "You left the game";
    }
    public String resign() throws ResponseException{
        ServerFacade server = client.getServer();
        String authToken = client.getAuthToken();
        int gameID = client.getGameID();
        server.resignGame(authToken, gameID);
        return "You resigned. Game over.";
    }

    public String handleCommand(String input) throws ResponseException, InvalidMoveException {
        String[] tokens = input.toLowerCase().split("\\s+");
        String cmd = (tokens.length > 0) ? tokens[0].toLowerCase(): "";
        ChessGame game = client.getGame();

        return switch(cmd){
            case "move" -> {
                if (tokens.length != 3) {
                    yield "Usage: move <from> <to>";
                }
                ChessPosition from = posConvert(tokens[1]);
                ChessPosition to = posConvert(tokens[2]);
                if (game == null){
                    yield "No game loaded. Try join <id> <color> first.";
                }
                game.makeMove(new ChessMove(from, to, game.getBoard().getPiece(from).getPieceType()));
                render();
                yield "";
            }
            case "board" -> {
                render();
                yield "";
            }
            case "resign" -> resign();
            case "leave"-> leave();
            case "help"-> client.help();
            case "homescreen"-> "Returning to homescreen.";
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
