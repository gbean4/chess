package client;

import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

import chess.ChessGame;
import chess.InvalidMoveException;
import datamodel.*;
import exception.ResponseException;
import server.ServerFacade;
import ui.GameUI;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNED_OUT;
    private String authToken = null;
    private String username = null;
    private GameUI gameUI = null;
    private ChessGame game = null;
    private String playerColor = null;
    private int gameID = -1;

    public ChessClient(ServerFacade serverFacade) {
        this.server = serverFacade;
    }

    public void setGameUI(GameUI gameUI){
        this.gameUI = gameUI;
    }
    public ChessGame getGame() {return game;}
    public String getPlayerColor(){return playerColor;}
    public int getGameID() {return gameID;}
    public String getAuthToken() {return authToken;}
    public ServerFacade getServer() {return server;}

    public void run() {
        System.out.println(LOGO + " Welcome to Chess! Type Help to get started.");
//        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("bye")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result + RESET_TEXT_COLOR);
            } catch (Throwable e) {
                System.out.print(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        String stateText = switch(state){
            case SIGNED_OUT -> "[SIGNED_OUT]";
            case SIGNED_IN -> "[SIGNED_IN]";
            case INGAME -> "[INGAME]";
        };
        System.out.print("\n" + RESET_TEXT_COLOR + stateText + " >>> " + SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split("\\s+");
            String cmd = (tokens.length > 0) ? tokens[0].toLowerCase(): "";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state == State.INGAME){
                String result = gameUI.handleCommand(input);
                if (result.equalsIgnoreCase("You resigned.") ||
                result.equalsIgnoreCase("You left the game.")){
                    state = State.SIGNED_IN;
                    gameUI = null;
                }
                return result;
            }

            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "list" -> listGames();
                case "create" -> createGame(params);
                case "join" -> joinGame(params);
                case "help" -> help();
                case "quit" -> "bye";
                default -> "Unknown command. Type 'help' for options.";
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length != 3) {
            return "Usage: register <username <email> <password>";
        }
        var user = new UserData(params[0],params[1], params[2]);
        var generateAuth = UUID.randomUUID().toString();
        var req = new RegisterResponse(user, user.username(), generateAuth);
        var res = server.register(req);
        username = res.username();
        authToken = res.authToken();
        state = State.SIGNED_IN;
        return String.format("Registered! Welcome, %s!", username);
    }

    public String login(String... params) throws ResponseException {
        if (params.length != 2) {
            return "Usage: login <username> <password>";
        }
        var req= new LoginRequest(params[0], params[1]);
        var res = server.login(req);
        username = res.username();
        authToken = res.authToken();
        state = State.SIGNED_IN;
        return String.format("Logged in! Welcome, %s!", username);
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout(authToken);
        state = State.SIGNED_OUT;
        authToken = null;
        return "BYE! Logged out successfully.";
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        var res = server.listGames(authToken);
        var out = new StringBuilder("Games:\n");
        for (GameData game : res.games()){
            out.append(String.format("  ID: %d | Name: %s | White: %s | Black: %s%n",
                    game.gameID(), game.gameName(), game.whiteUsername(), game.blackUsername()));
        }
        return out.toString();
    }

    public String createGame(String ... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            return "Usage: create <gameName>";
        }
        var req = new CreateGameRequest(params[0]);
        int gameID = server.createGame(req, authToken);
        return String.format("Game created with ID %d", gameID);
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 2) {
            return "Usage: join <gameID> <WHITE|BLACK|OBSERVER>";
        }
        int gameID = Integer.parseInt(params[1]);
        String playerColor = params[0].toUpperCase();
        var spec = new GameSpec(playerColor, gameID);
        var gameData = server.joinGame(spec);

        state = State.INGAME;
        this.game = gameData.game();
        this.playerColor = playerColor;
        this.gameID = gameID;

        if (this.gameUI == null){
            this.gameUI = new GameUI(this);
            this.gameUI.render();
        } else{
            this.gameUI.render();
        }

        return String.format("Joined game %d as %s", gameID, playerColor);
    }

//    private void leave() throws ResponseException{
//        assertSignedIn();
//        if (state != State.INGAME){
//            throw new ResponseException(ResponseException.Code.ClientError, "You are not currently in a game.");
//        }
//        server.leaveGame(authToken, gameUI.getGameID());
//        state = State.SIGNED_IN;
//        System.out.println("You left the game");
//    }
//
//    private void resign() throws ResponseException{
//        assertSignedIn();
//        if (state != State.INGAME){
//            throw new ResponseException(ResponseException.Code.ClientError, "You are not currently in a game.");
//        }
//        server.leaveGame(authToken, gameUI.getGameID());
//        state = State.SIGNED_IN;
//        System.out.println("You resigned. Game over.");
//    }

    public String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    Commands:
                    register <username> <email> <password> - to create an account
                    login <username> <password> - to play chess
                    help - with possible commands
                    quit - playing chess
                    """;
        } else if (state == State.SIGNED_IN){
            return """
                    Commands:
                    list - show all games
                    create <gameName> - create your own
                    join <gameID> <WHITE|BLACK|OBSERVER> - join a game or observe
                    logout - end session
                    quit - playing chess
                    """;
        } else{
            return """
                    Commands:
                    move <from> <to> - make move
                    board - render board
                    leave - let someone else take your spot
                    resign - give up :(
                    quit - playing chess
                    """;
        }
    }

    private enum State{
        SIGNED_OUT,
        SIGNED_IN,
        INGAME,
    }

    public static final String LOGO = """
    ♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜
    ♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟
    ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙
    ♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖
""";

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
        }
    }
}