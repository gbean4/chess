package service;

import dataaccess.MemoryDataAccess;
import datamodel.GameData;
import datamodel.GameSpec;
import datamodel.RegisterResponse;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void constructorPositive(){
        var db = new MemoryDataAccess();
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
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        RegisterResponse response = service.register(user);

        assertNotNull(response);
        assertEquals("lee", response.username());
        assertNotNull(response.authToken(), "authToken should not be null");
    }



    @Test
    void registerNegative() throws Exception {
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");
        service.register(user);

        Exception ex = assertThrows(Exception.class, () -> service.register(user));
        assertTrue(ex.getMessage().contains("exists"));
    }

    @Test
    void loginPositive() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);

        var auth = service.login("lee", "password");
        assertNotNull(auth);
        assertEquals("lee", auth.username());
        assertNotNull(db.getAuth(auth.authToken()));
     }

    @Test
    void loginNegative() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);

        Exception ex = assertThrows(Exception.class, () -> service.login("lee", "wrongPass"));
        assertTrue(ex.getMessage().contains("Invalid"));
    }

    @Test
    void logoutPositive() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);
        var auth = service.login("lee", "password");

        service.logout(auth.authToken());
        assertNull(db.getAuth(auth.authToken()));
    }

    @Test
    void logoutNegative(){
        var db = new MemoryDataAccess();
        var service = new UserService(db);

        Exception ex = assertThrows(Exception.class, () -> service.logout("badToken"));
        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    void createGamePositive() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);
        var auth = service.login("lee", "password");

        var game = service.createGame(auth.authToken(), "Chess Game");
        assertNotNull(game);
        assertEquals("Chess Game", game.gameName());
    }

    @Test
    void createGameNegative() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);

        Exception ex = assertThrows(Exception.class, () -> service.createGame("badToken", "Chess Game"));
        assertTrue(ex.getMessage().contains("401"));
    }

    @Test
    void listGamesPositive() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);
        var auth = service.login("lee", "password");
        service.createGame(auth.authToken(), "Game1");
        var games = service.listGames(auth.authToken());

        assertNotNull(games);
        assertTrue(games.length>= 1);
    }

    @Test
    void listGamesNegative(){
        var db = new MemoryDataAccess();
        var service = new UserService(db);

        Exception ex = assertThrows(Exception.class, () -> service.listGames("badToken"));
        assertTrue(ex.getMessage().contains("401"));
    }
    @Test
    void joinGamePositive() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);
        var auth = service.login("lee", "password");
        var game = service.createGame(auth.authToken(), "Game1");

        var spec = new GameSpec("white", game.gameID());
        assertDoesNotThrow(() -> service.joinGame(auth.authToken(), spec));
    }

    @Test
    void joinGameNegative() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);
        var auth = service.login("lee", "password");
        var game = service.createGame(auth.authToken(), "busyGame");

        var fullGame = new GameData(game.gameID(), "a", "b", "busyGame", game.game());
        var spec = new GameSpec("white", game.gameID());

        db.updateGame(fullGame);

        Exception ex = assertThrows(Exception.class, () -> service.joinGame(auth.authToken(), spec));
        assertTrue(ex.getMessage().contains("already"));

    }

}