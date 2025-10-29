package dataaccess;

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
    void deleteAuthNegative() throws ResponseException {
        var user = new UserData("Max", "a@c.com", "pwd");
        db.createUser(user);
        String authToken = UUID.randomUUID().toString();
        db.createAuth(new AuthData(user.username(), authToken));
        var ex = assertThrows(ResponseException.class, ()-> db.deleteAuth("wrongAuth"));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
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
}