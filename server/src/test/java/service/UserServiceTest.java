package service;

import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void register() throws Exception {
        var db = new MemoryDataAccess();
        var service = new UserService(db);
        var user = new UserData("lee", "2@c","password");

        assertDoesNotThrow(() ->service.register(user));
        service.register(user);
//        assertNotNull(a.authToken());
    }
}