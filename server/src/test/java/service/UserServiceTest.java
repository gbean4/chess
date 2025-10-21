package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.RegisterResponse;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void register() throws Exception {
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        RegisterResponse response = service.register(user);

        assertNotNull(response);
        assertEquals("lee", response.username());
        assertNotNull(response.authToken(), "authToken should not be null");
        var storedAuth = db.getAuth(response.authToken());
        assertNotNull(storedAuth, "oh man it's not stored in db");
        assertEquals("lee", storedAuth.username());
        System.out.println(storedAuth.authToken().equals(response.authToken()));
    }
    @Test
    void login() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);

        var firstAuth = service.login("lee", "password");
        assertNotNull(firstAuth);
        assertNotNull(firstAuth.authToken());

        var secondAuth = service.login("lee", "password");
        assertNotEquals(firstAuth.authToken(), secondAuth.authToken(), "authTokens should be different");
    }

    @Test
    void logout() throws Exception{
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        service.register(user);
        var auth = service.login("lee", "password");

        assertNotNull(db.getAuth(auth.authToken()), "there's nothing in db");
        service.logout(auth.authToken());
        db.listAuth();
        assertNull(db.getAuth(auth.authToken()), "there should be NO auth token");
        db.listAuth();
    }

}