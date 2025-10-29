package service;
import dataaccess.MySqlDataAccess;
import datamodel.GameSpec;
import datamodel.RegisterResponse;
import datamodel.UserData;
import exception.DataAccessException;
import exception.ResponseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void constructorPositive() throws ResponseException, DataAccessException {
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        assertNotNull(service, "Service should be all good");
    }

    @Test
    void constructorNegative(){
        var service = new UserService(null);
        assertNotNull(service, "Service should not work, db null");
    }

    @Test
    void registerPositive() throws Exception{
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        var user = new UserData("Bella", "gbean@c","pass");

        RegisterResponse response = service.register(user);

        assertNotNull(response);
        assertEquals("Bella", response.username());
        assertNotNull(response.authToken(), "authToken should not be null");
    }



    @Test
    void registerNegative() throws Exception {
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        var user = new UserData("Bree", "love@c","pass");
        service.register(user);

        Exception ex = assertThrows(Exception.class, () -> service.register(user));
        assertTrue(ex.getMessage().contains("exists"));
    }

    @Test
    void loginPositive() throws Exception{
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        var user = new UserData("another", "account@c","yay");

        service.register(user);

        var auth = service.login("another", "yay");
        assertNotNull(auth);
        assertEquals("another", auth.username());
        assertNotNull(db.getAuth(auth.authToken()));
     }

    @Test
    void loginNegative() throws Exception{
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        var user = new UserData("mary", "serene@c","password");

        service.register(user);

        Exception ex = assertThrows(Exception.class, () -> service.login("mary", "wrongPass"));
        assertTrue(ex.getMessage().contains("Invalid"));
    }

    @Test
    void logoutPositive() throws Exception{
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        var user = new UserData("ben", "jamin@c","password");

        service.register(user);
        var auth = service.login("ben", "password");

        service.logout(auth.authToken());
        assertNull(db.getAuth(auth.authToken()));
    }

    @Test
    void logoutNegative() throws ResponseException, DataAccessException {
        var db = new MySqlDataAccess();
        var service = new UserService(db);

        Exception ex = assertThrows(Exception.class, () -> service.logout("badToken"));
        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    void createGamePositive() throws Exception{
        var db = new MySqlDataAccess();
        db.clear();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);
        var auth = service.login("lee", "password");

        int gameID = service.createGame(auth.authToken(), "Chess Game");
        var createdGame = db.getGame(gameID);
        assertNotNull(createdGame);
        assertEquals("Chess Game", createdGame.gameName());
    }

    @Test
    void createGameNegative() throws Exception{
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        var user = new UserData("pumpkin", "yo@c","password");

        service.register(user);

        Exception ex = assertThrows(Exception.class, () -> service.createGame("badToken", "Chess Game"));
        assertTrue(ex.getMessage().contains("401"));
    }

    @Test
    void listGamesPositive() throws Exception{
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        var user = new UserData("please", "pass@c","password");

        service.register(user);
        var auth = service.login("please", "password");
        service.createGame(auth.authToken(), "Game1");
        var games = service.listGames(auth.authToken());

        assertNotNull(games);
        assertTrue(games.length>= 1);
    }

    @Test
    void listGamesNegative() throws ResponseException, DataAccessException {
        var db = new MySqlDataAccess();
        var service = new UserService(db);

        Exception ex = assertThrows(Exception.class, () -> service.listGames("badToken"));
        assertTrue(ex.getMessage().contains("401"));
    }
    @Test
    void joinGamePositive() throws Exception{
        var db = new MySqlDataAccess();
        db.clear();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);
        var auth = service.login("lee", "password");
        var gameID = service.createGame(auth.authToken(), "Game1");

        var spec = new GameSpec("white", gameID);
        assertDoesNotThrow(() -> service.joinGame(auth.authToken(), spec));
    }

    @Test
    void joinGameNegative() throws Exception{
        var db = new MySqlDataAccess();
        var service = new UserService(db);
        var user1 = new UserData("john", "2@c","password");
        var user2 = new UserData("Steph", "3@c", "password");

        service.register(user1);
        service.register(user2);

        var auth1 = service.login("john", "password");
        var auth2 = service.login("Steph", "password");

        var gameID = service.createGame(auth1.authToken(), "busyGame");

        service.joinGame(auth1.authToken(), new GameSpec("white", gameID));

        Exception ex = assertThrows(Exception.class,
                () -> service.joinGame(auth2.authToken(), new GameSpec("white", gameID)));
        assertTrue(ex.getMessage().contains("already"));

    }

}