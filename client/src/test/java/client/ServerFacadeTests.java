package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void registerPositive() {
    }
    @Test
    void registerNegative() {
    }

    @Test
    void loginPositive() {
    }
    @Test
    void loginNegative() {
    }

    @Test
    void logoutPositive() {
    }
    @Test
    void logoutNegative() {
    }

    @Test
    void clearPositive() {
    }
    @Test
    void clearNegative() {
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

}
