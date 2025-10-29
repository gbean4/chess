package dataaccess;

import datamodel.*;
import exception.DataAccessException;
import exception.ResponseException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @Test
    void clear() throws ResponseException, DataAccessException {
        var db = new MySqlDataAccess();
        final var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());
        assertEquals(user, resultingUser);

        db.clear();
        assertNull(db.getUser(user.username()));
    }

    @Test
    void createUser() throws ResponseException, DataAccessException {
        var db = new MySqlDataAccess();
        final var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());
        assertEquals(user, resultingUser);
        assertEquals(user.password(), resultingUser.password());
    }

    @Test
    void getUser() {
    }
}