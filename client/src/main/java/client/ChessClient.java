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

import static java.awt.Color.*;
import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNED_OUT;
    private String authToken = null;
    private String username = null;
    private GameUI gameUI = null;

    public ChessClient(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(LOGO + " Welcome to Chess!");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(BLUE + result + RESET_TEXT_COLOR);
            } catch (Throwable e) {
                System.out.print(RED + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + GREEN);
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
                case "quit" -> "quit";
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
            return "Usage: register <username <password> <email>";
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
        String playerColor = params[0].toLowerCase();
        var spec = new GameSpec(playerColor, gameID);

        var gameData = server.joinGame(spec);
        ChessGame game = gameData.game();
        state = State.INGAME;

        gameUI = new GameUI(game, spec.playerColor(), server, authToken, gameID);
        gameUI.render();

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
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    """;
        } else if (state == State.SIGNED_IN){
            return """
                    Commands:
                    - list
                    - create <gameName>
                    - join <gameID> <WHITE|BLACK|OBSERVER>
                    - logout
                    - quit
                    """;
        } else{
            return """
                    Commands:
                    - move <from> <to>
                    - board
                    - leave
                    - resign
                    - quit
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