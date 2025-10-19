package dataaccess;

import datamodel.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @Test
    void clear() {
        var db = new MemoryDataAccess();
        final var user = new UserData("chase", "c@c.com", "pwd");
        db.createUser(user);
        var resultingUser = db.getUser(user.username());
        assertEquals(user, resultingUser);

        db.clear();
        assertNull(db.getUser(user.username()));
    }

    @Test
    void createUser() {
        var db = new MemoryDataAccess();
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