package ui;


import chess.*;
import client.ChessClient;
import exception.ResponseException;
import server.ServerFacade;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;


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
        ChessBoardUI.renderBoard(game, perspective, null, null);
    }

    public String highlightMoves(String...params){
        ChessGame.TeamColor perspective= null;
        String playerColor = client.getPlayerColor();
        ChessGame game = client.getGame();
        if (params.length != 1) {
            return "Usage: highlight <position>";
        }
        ChessPosition pos = posConvert(params[0]);
        if (!game.getBoard().inBounds(pos)){
            return "Not a real position. Enter letter column first and then row like 'a1'";
        }
        if (playerColor != null){
            if (playerColor.equalsIgnoreCase("white")){
                perspective = ChessGame.TeamColor.WHITE;
            } else if (playerColor.equalsIgnoreCase("black")){
                perspective = ChessGame.TeamColor.BLACK;
            }
        }
        Set<ChessPosition> moves = ChessBoardUI.getHighlightSquares(game,pos);

        ChessBoardUI.renderBoard(game, perspective, pos, moves);
        return "Moves for " + params[0];
    }

    public String leave() throws ResponseException{
        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE,
                client.getAuthToken(), client.getGameID());
        client.getWebsocket().sendCommand(cmd);
        return "You left the game.";
    }
    public String resign() throws ResponseException{
        Scanner scanner = new Scanner(System.in);
        System.out.print("Are you sure you want to resign? (y/n)");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (!answer.equals("y") && !answer.equals("yes")){
            return "Resign cancelled. Keep playing!";
        }

        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN,
                client.getAuthToken(), client.getGameID());
        client.getWebsocket().sendCommand(cmd);
        return "Resignation sent to server... Don't cry";
    }

    public String move(String... params) throws InvalidMoveException, ResponseException {
        if (params.length != 2) {
            return "Usage: move <from> <to>";
        }
        ChessPosition from = posConvert(params[0]);
        ChessPosition to = posConvert(params[1]);

        UserGameCommand cmd = new MakeMoveCommand(client.getAuthToken(), client.getGameID(), new ChessMove(from, to, null));
        client.getWebsocket().sendCommand(cmd);
        return "Move sent...";
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
            case "highlight"-> highlightMoves(params);
            case "back"-> "Returning to homescreen.";
            default -> "Unknown command. Type help for available commands.";
        };
    }

    public static ChessPosition posConvert(String pos){
        char file = pos.charAt(0);
        char rank = pos.charAt(1);

        int col = (file - 'a')+1;
        int row = rank - '0';
        return new ChessPosition(row,col);
    }
}
