package dataaccess;

import datamodel.*;
import exception.DataAccessException;
import exception.ResponseException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

class DataAccessTest {
    private MySqlDataAccess db;

    @BeforeEach
    void setup() throws ResponseException, DataAccessException{
        db = new MySqlDataAccess();
        db.clear();
    }

    @Test
    void clearPositive() throws ResponseException, DataAccessException {
        var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());

        assertNotNull(resultingUser);
        assertEquals(user, resultingUser);
        assertEquals("chase", resultingUser.username());
    }

    @Test
    void clearNegative() throws ResponseException, DataAccessException {
        var user1 = new UserData("john", "2@c","password");
        var user2 = new UserData("john", "2@c", "diffPassword");
        db.createUser(user1);

        var ex = assertThrows(RuntimeException.class, ()-> db.createUser(user2));
        assertTrue(ex.getMessage().toLowerCase().contains("duplicate") ||
                ex.getMessage().toLowerCase().contains("constraint"));
    }
    @Test
    void createUserPositive() throws ResponseException, DataAccessException {
        final var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());
        assertEquals(user, resultingUser);
        assertEquals(user.password(), resultingUser.password());
    }

    @Test
    void createUserNegative() throws ResponseException, DataAccessException {
        final var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());
        assertEquals(user, resultingUser);
        assertEquals(user.password(), resultingUser.password());
    }

    @Test
    void getUserPositive() {
    }

    @Test
    void getUserNegative() {
    }

    @Test
    void createAuthPositive() {
    }

    @Test
    void createAuthNegative() {
    }

    @Test
    void getAuthPositive() {
    }

    @Test
    void getAuthNegative() {
    }

    @Test
    void deleteAuthPositive() {
    }

    @Test
    void deleteAuthNegative() {
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