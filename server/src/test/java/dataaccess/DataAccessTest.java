package dataaccess;

import chess.ChessGame;
import datamodel.*;
import exception.DataAccessException;
import exception.ResponseException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class DataAccessTest {
    private MySqlDataAccess db;

    @BeforeEach
    void setup() throws ResponseException, DataAccessException{
        db = new MySqlDataAccess();
        db.clear();
    }

    @Test
    void clearPositive() throws ResponseException {
        var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());

        assertNotNull(resultingUser);
        assertEquals(user, resultingUser);
        assertEquals("chase", resultingUser.username());
    }

    @Test
    void clearNegative(){
        var user1 = new UserData("john", "2@c","password");
        var user2 = new UserData("john", "2@c", "diffPassword");
        db.createUser(user1);

        var ex = assertThrows(RuntimeException.class, ()-> db.createUser(user2));
        assertTrue(ex.getMessage().toLowerCase().contains("duplicate") ||
                ex.getMessage().toLowerCase().contains("constraint"));
    }
    @Test
    void createUserPositive() throws ResponseException{
        var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());
        assertEquals(user, resultingUser);
        assertEquals(user.password(), resultingUser.password());
    }

    @Test
    void createUserNegative() throws ResponseException{
        var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());
        assertEquals(user, resultingUser);
        assertEquals(user.password(), resultingUser.password());
    }

    @Test
    void getUserPositive() throws ResponseException {
        var user = new UserData("Adam", "a@c.com", "pwd");
        db.createUser(user);
        var getData = db.getUser(user.username());
        assertEquals(user, getData);
    }

    @Test
    void getUserNegative() throws ResponseException {
        var user = new UserData("Adam", "a@c.com", "pwd");
        var getData = db.getUser(user.username());
        assertNull(getData);
    }

    @Test
    void createAuthPositive() throws ResponseException {
        var user = new UserData("Adam", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        var foundAuth = db.getAuth(authToken);
        assertNotNull(foundAuth);
        assertEquals(user.username(), foundAuth.username());
    }

    @Test
    void createAuthNegative() throws ResponseException {
        var user = new UserData("Adam", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        var foundAuth = db.getAuth(authToken);
        var ex = assertThrows(RuntimeException.class, ()-> db.createAuth(foundAuth));
        assertTrue(ex.getMessage().toLowerCase().contains("duplicate") ||
                ex.getMessage().toLowerCase().contains("constraint"));
    }

    @Test
    void getAuthPositive() throws ResponseException {
        var user = new UserData("Max", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        var foundAuth = db.getAuth(authToken);
        assertNotNull(foundAuth);
    }

    @Test
    void getAuthNegative() throws ResponseException {
        var foundAuth = db.getAuth("notRealAuth");
        assertNull(foundAuth);
    }

    @Test
    void deleteAuthPositive() throws ResponseException {
        var user = new UserData("Max", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        db.deleteAuth(authToken);
        assertNull(db.getAuth(authToken));
    }

    @Test
    void deleteAuthNegative(){
        var user = new UserData("Max", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        var ex = assertThrows(ResponseException.class, ()-> db.deleteAuth("wrongAuth"));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void createGamePositive() throws ResponseException {
        int gameID = db.createGame("newGameName");
        var foundGame = db.getGame(gameID);
        assertEquals(foundGame.gameID(), gameID);
    }

    @Test
    void createGameNegative() throws ResponseException {
        int gameID = db.createGame("newGameName");
        var foundGame = db.getGame(gameID);
        assertEquals(foundGame.gameID(), gameID);
    }

    @Test
    void listGamesPositive() throws ResponseException {
        var user = new UserData("Max", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        int game1 = db.createGame("newGame1");
        int game2 = db.createGame("newGame2");
        assertNotNull(db.listGames(authToken));
    }

    @Test
    void listGamesNegative() throws ResponseException {
        var user = new UserData("Max", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        assertNotNull(db.listGames(authToken));
    }

    @Test
    void joinGamePositive() throws ResponseException {
        var user = new UserData("Max", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        int game1 = db.createGame("newGame1");
        int game2 = db.createGame("newGame2");

        var mySpec = new GameSpec("white", game1);
        db.joinGame(user.username(), mySpec);
        assertEquals(user.username(), db.getGame(game1).whiteUsername());
    }

    @Test
    void joinGameNegative() throws ResponseException {
        var user = new UserData("Max", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        int game1 = db.createGame("newGame1");
        int game2 = db.createGame("newGame2");

        var mySpec = new GameSpec("green", game1);

        var ex = assertThrows(ResponseException.class, ()-> db.joinGame(user.username(), mySpec));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }

    @Test
    void getGamePositive() throws ResponseException {
        int gameID = db.createGame("newGameName");
        var foundGame = db.getGame(gameID);
        assertEquals(foundGame.gameID(), gameID);
    }

    @Test
    void getGameNegative() throws ResponseException {
        int gameID = db.createGame("newGameName");
        var foundGame = db.getGame(222);
        assertNull(foundGame);
    }
}