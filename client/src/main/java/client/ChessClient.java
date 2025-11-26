package client;

import java.util.*;

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
    private final Map<Integer, Integer> tempToRealIDs = new HashMap<>();

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
                if (result.equalsIgnoreCase("You resigned. Game over.") ||
                result.equalsIgnoreCase("You left the game.")||
                        result.equalsIgnoreCase("Returning to homescreen.")){
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
                case "play" -> playGame(params);
                case "join" -> joinGame(params);
                case "observe" ->observeGame(params);
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
        var req = new RegisterRequest(user.username(), user.email(), user.password());
        var res = server.register(req);
        username = res.username();
        authToken = res.authToken();
        state = State.SIGNED_IN;
        return String.format("Registered! Welcome, %s!", username);
    }

    public String login(String... params) throws ResponseException {
        if (state!= State.SIGNED_OUT) {
            return "Silly goose, you're already logged in! Logout first.";
        }
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
        tempToRealIDs.clear();

        var out = new StringBuilder("Games:\n");
        int tempID = 1;
        for (GameData game : res.games()){
            tempToRealIDs.put(tempID, game.gameID());
            out.append(String.format("  ID: %d | Name: %s | White: %s | Black: %s%n",
                    tempID, game.gameName(), game.whiteUsername(), game.blackUsername()));
            tempID++;
        }
        return out.toString();
    }

    public String createGame(String ... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            return "Usage: create <gameName>";
        }
        var req = new CreateGameRequest(params[0]);
        server.createGame(req, authToken);
        return "Game created. Type List to check its ID to join";
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 2) {
            return "Usage: join <gameID> <WHITE|BLACK>";
        }
        int tempID;
        try{
            tempID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e){
            return "Invalid game ID format. Try again.";
        }
        if (!tempToRealIDs.containsKey(tempID)){
            return "No such game number in your list. Try 'list' first.";
        }

        int gameID = tempToRealIDs.get(tempID);
        String playerColor = params[1].toUpperCase();

        if (!playerColor.equals("WHITE")&& !playerColor.equals(("BLACK"))){
            return "Invalid color. Use WHITE or BLACK";
        }

        ListGamesResponse listResponse = server.listGames(authToken);
        GameData targetGame = null;
        for (GameData g : listResponse.games()){
            if (g.gameID() == gameID){
                targetGame = g;
                break;
            }
        }

        if (targetGame == null){
            return "Game not found on server. Try 'list' again to refresh.";
        }
        if (targetGame.whiteUsername() !=null && playerColor.equals("WHITE")){
            return "Sorry! White is already taken.";
        } else if (targetGame.blackUsername() !=null && playerColor.equals("BLACK")){
            return "Sorry! Black is already taken.";
        }

        var spec = new GameSpec(playerColor, gameID);
        var gameData = server.joinGame(spec, authToken);

        var fullGame = server.getGame(authToken,gameID);
        this.game = fullGame.game();
        this.gameID = gameID;
        this.playerColor = playerColor;
        if (this.gameUI == null){
            this.gameUI = new GameUI(this);
        }

        this.gameUI.render();
        state = State.INGAME;
        return String.format("Joined game %d as %s.", tempID, playerColor);
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            return "Usage: observe <gameID>";
        }
        int tempID;
        try{
            tempID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e){
            return "Invalid game ID format.";
        }
        if (!tempToRealIDs.containsKey(tempID)){
            return "No such game number in your list. Try 'list' first.";
        }
        int gameID = tempToRealIDs.get(tempID);
        var spec = new GameSpec(null, gameID);
//        var gameData = server.joinGame(spec, authToken);

        var fullGame = server.getGame(authToken,gameID);
        if (fullGame== null){
            return "No one is here yet! Wait till someone joins.";
        }
        this.game = fullGame.game();
        this.gameID = gameID;
        this.playerColor = null;

        if (this.gameUI == null){
            this.gameUI = new GameUI(this);
        }
        this.gameUI.render();
        state = State.INGAME;
        return String.format("Observing game %d", tempID);
    }

    public String playGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            return "Usage: play <gameID>";
        }
        int tempID = 0;
        try {
            tempID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException(
                    "Invalid ID: " + tempID+". Use a number." );
        }
        Integer gameID = tempToRealIDs.get(tempID);
        if (gameID == null){
            throw new ResponseException(
                    "Invalid ID: " + tempID+". Try 'list' again." );
        }
        ListGamesResponse listResponse = server.listGames(authToken);
        GameData targetGame = null;
        for (GameData g : listResponse.games()){
            if (g.gameID() == gameID){
                targetGame = g;
                break;
            }
        }

        if (targetGame == null){
            throw new ResponseException(
                    "Game ID " + tempID + " not found.");
        }

        if (username.equals(targetGame.whiteUsername())){
            playerColor = "white";
        } else if(username.equals(targetGame.blackUsername())){
            playerColor = "black";
        } else{
            throw new ResponseException("You are not a player in this game. Join it first!");
        }
        GameData fullGame = server.getGame(authToken, gameID);
        if (fullGame == null|| fullGame.game()==null){
            fullGame = new GameData(gameID, targetGame.whiteUsername(), targetGame.blackUsername(), targetGame.gameName(), new ChessGame());
        }

        gameModeAndRender(gameID, fullGame, playerColor);

        return String.format("Playing game %d as %s", tempID, playerColor);
    }

    private void gameModeAndRender(int gameID, GameData targetGame, String playerColor) {
        state = State.INGAME;
        this.game = targetGame.game();
        this.playerColor = playerColor;
        this.gameID = gameID;

        if (this.gameUI == null){
            this.gameUI = new GameUI(this);
        }
        this.gameUI.render();
    }

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
                    join <gameID> <WHITE|BLACK> - join a game
                    observe <gameID> - observe a game
                    play <gameID>
                    logout - end session
                    help - redisplay these commands
                    quit - playing chess
                    """;
        } else{
            return """
                    Commands:
                    move <from> <to> - make move
                    board - render board
                    highlight moves <from> - show available moves
                    leave - let someone else take your spot
                    resign - give up :(
                    logout - end session
                    help - redisplay these commands
                    back - return to beginning
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
            throw new ResponseException("You must sign in");
        }
    }

}