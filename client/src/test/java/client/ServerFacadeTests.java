package client;

import datamodel.*;
import exception.ResponseException;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        String serverUrl = "http://localhost:" + port;
        facade = new ServerFacade(serverUrl);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDB() throws ResponseException{
        facade.clear();
    }

    @Test
    void registerPositive() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);

        assertNotNull(response);
        assertNotNull(response.authToken());
        assertEquals("user1", response.username());
    }

    @Test
    void registerNegative(){
        var request = new RegisterRequest("","","");
        assertThrows(ResponseException.class,()-> facade.register(request));
    }

    @Test
    void loginPositive() throws ResponseException {
        var request = new RegisterRequest("user1","e@e.com","password");
        RegisterResponse response = facade.register(request);


        assertNotNull(response);
        assertNotNull(response.authToken());
        assertEquals("user1", response.username());

        var loginRequest = new LoginRequest("user1", "password");
        var authData = facade.login(loginRequest);

        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals("user1", authData.username());
    }
    @Test
    void loginNegative() {
        var loginReq = new LoginRequest("nonexistent", "wrong");
        assertThrows(ResponseException.class,()-> facade.login(loginReq));
    }

    @Test
    void logoutPositive() throws ResponseException {
        var request = new RegisterRequest("user1","e@e.com","password");
        RegisterResponse response = facade.register(request);
        assertDoesNotThrow(()-> facade.logout(response.authToken()));
    }
    @Test
    void logoutNegative() {
        assertThrows(ResponseException.class, ()->facade.logout("rando"));
    }

    @Test
    void clearPositive() {
        assertDoesNotThrow(()-> facade.clear());
    }

    @Test
    void clearNegative() {
        ServerFacade badFacade = new ServerFacade("https://localhost:9999/");
        assertThrows(ResponseException.class, badFacade::clear);
    }

    @Test
    void createGamePositive() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        var req = new CreateGameRequest("Cool Game");
        assertDoesNotThrow(()-> facade.createGame(req, response.authToken()));
    }
    @Test
    void createGameNegative() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        var req = new CreateGameRequest("");
        assertThrows(ResponseException.class, ()-> facade.createGame(req, response.authToken()));
        assertThrows(ResponseException.class, ()-> facade.createGame(new CreateGameRequest("GameName"), "noAuth"));
    }

    @Test
    void listGamesPositive() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        var list = facade.listGames(response.authToken());
        assertNotNull(list);
        assertNotNull(list.games());
    }
    @Test
    void listGamesNegative() throws ResponseException {
        assertThrows(ResponseException.class, ()-> facade.listGames("noAuth"));
    }

    @Test
    void joinGamePositive() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        var req = new CreateGameRequest("Cool Game");
        facade.createGame(req, response.authToken());
        var list = facade.listGames(response.authToken());
        var game = list.games()[0];
        var joinSpec = new GameSpec("WHITE", game.gameID());
        var joined = facade.joinGame(joinSpec, response.authToken());
        assertNotNull(joined);
//        assertEquals(game.gameID(), joined.gameID());
    }

    @Test
    void joinGameNegative() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        var joinSpec = new GameSpec("WHITE", 999);
        assertThrows(ResponseException.class, ()-> facade.joinGame(joinSpec, response.authToken()));
    }

    @Test
    void getGamePositive() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        var req = new CreateGameRequest("Cool Game");
        facade.createGame(req, response.authToken());

        var list = facade.listGames(response.authToken());
        var gameID = list.games()[0].gameID();

        var fetched = facade.getGame(response.authToken(), gameID);
        assertNotNull(fetched);
        assertEquals(gameID, fetched.gameID());
    }
    @Test
    void getGameNegative() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        assertThrows(ResponseException.class, ()-> facade.getGame(response.authToken(), 999));
    }

    @Test
    void leaveGamePositive() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        var req = new CreateGameRequest("Cool Game");
        facade.createGame(req, response.authToken());

        var list = facade.listGames(response.authToken());
        var gameID = list.games()[0].gameID();

        facade.joinGame(new GameSpec("WHITE", gameID), response.authToken());
        assertDoesNotThrow(()-> facade.leaveGame(response.authToken(), gameID));
    }

    @Test
    void leaveGameNegative() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        assertThrows(ResponseException.class, ()-> facade.leaveGame(response.authToken(), 99));
    }

    @Test
    void resignGamePositive() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        var req = new CreateGameRequest("Cool Game");
        facade.createGame(req, response.authToken());

        var list = facade.listGames(response.authToken());
        var gameID = list.games()[0].gameID();

        facade.joinGame(new GameSpec("WHITE", gameID), response.authToken());
        assertDoesNotThrow(()-> facade.resignGame(response.authToken(), gameID));
    }

    @Test
    void resignGameNegative() throws ResponseException {
        var request = new RegisterRequest("user1","email@example.com","password");
        RegisterResponse response = facade.register(request);
        assertThrows(ResponseException.class, ()-> facade.resignGame(response.authToken(), 99));
    }
}
