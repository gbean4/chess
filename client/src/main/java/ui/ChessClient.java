package ui;

import java.util.Arrays;
import java.util.Scanner;

import com.google.gson.Gson;
//import model.*;
import datamodel.AuthData;
import datamodel.LoginRequest;
import datamodel.RegisterResponse;
import datamodel.UserData;
import exception.ResponseException;
import server.ServerFacade;

import static java.awt.Color.*;
//import client.websocket.NotificationHandler;

//import client.websocket.WebSocketFacade;
//import webSocketMessages.Notification;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNED_OUT;
    private String authToken = null;
    private String username = null;

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
                System.out.print(BLUE + result + RESET);
            } catch (Throwable e) {
                System.out.print(RED + e.getMessage() + RESET);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split("\\s+");
            String cmd = (tokens.length > 0) ? tokens[0].toLowerCase(): "";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "list" -> listGames();
                case "create" -> createGame(params);
                case "join" -> joinHame(params);
                case "help" -> help();
                case "quit" -> "quit";
                default -> "Unknown command. Type 'help' for options.";
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(UserData user) throws ResponseException {
        if (user == null) {
            return "Usage: register <username <password> <email>";
        }
        var req = new RegisterResponse(user, user.username(), authToken);
        var res = server.register(req);
        username = res.username();
        authToken = res.authToken();
        state = State.SIGNED_IN;
        return String.format("Registered! Welcome, %s!", username);
    }

    public String login(LoginRequest userReq) throws ResponseException {
        if (userReq == null) {
            return "Usage: login <username> <password>";
        }
        var req= new LoginRequest(userReq.username(), userReq.password());
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
                    - resign
                    - logout
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