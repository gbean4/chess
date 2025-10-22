package service;

import dataaccess.MemoryDataAccess;
import datamodel.RegisterResponse;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

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

        RegisterResponse response = service.register(user);

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

        Exception ex = assertThrows(Exception.class, () -> service.login("lee", "wrongpass"));
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
    void logoutNegative() throws Exception{
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
    void listGamesNegative() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");


        Exception ex = assertThrows(Exception.class, () -> service.listGames("badToken"));
        assertTrue(ex.getMessage().contains("401"));
    }


}