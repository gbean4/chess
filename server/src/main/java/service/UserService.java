package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.AuthData;
import datamodel.RegisterResponse;
import datamodel.UserData;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public RegisterResponse register(UserData user) throws Exception {
        var existingUser = dataAccess.getUser(user.username());
        if (user.username()==null || user.password()==null ||user.email()==null){
            throw new Exception("Missing required fields");
        }
        if (existingUser != null){
//            throw new Exception("User already exists");
            throw new DataAccessException("User already exists");
        }

        dataAccess.createUser(user);
        return new RegisterResponse(user, user.username(),  "zyz");
    }

    public AuthData login(String username, String password) throws Exception {
        if (username == null || password ==null){
            throw new Exception("400: Missing fields");
        }
        UserData user = dataAccess.getUser(username);
        if (user == null){
            throw new Exception("401: User not found");
        }
        if (!user.password().equals(password)){
            throw new Exception("401: Invalid password");
        }

        String token = UUID.randomUUID().toString();

        return new AuthData(username, token);
    }
}
