package client;

import datamodel.LoginRequest;
import datamodel.RegisterRequest;
import datamodel.RegisterResponse;
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
    void createGamePositive() {
    }
    @Test
    void createGameNegative() {
    }

    @Test
    void listGamesPositive() {
    }
    @Test
    void listGamesNegative() {
    }

    @Test
    void joinGamePositive() {
    }
    @Test
    void joinGameNegative() {
    }

    @Test
    void getGamePositive() {
    }
    @Test
    void getGameNegative() {
    }

    @Test
    void leaveGamePositive() {
    }
    @Test
    void leaveGameNegative() {
    }

    @Test
    void resignGamePositive() {
    }
    @Test
    void resignGameNegative() {
    }


}
