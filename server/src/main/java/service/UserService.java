package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.AuthData;
import datamodel.GameData;
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
        var token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(user.username(), token));
        return new RegisterResponse(user, user.username(), token );
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
        var auth = new AuthData(username, token);
        dataAccess.createAuth(auth);
        return auth;
    }

    public void logout(String authToken) throws Exception {
        if (authToken == null){
            throw new Exception("unauthorized");
        }

        var existingAuth= dataAccess.getAuth(authToken);
        if (existingAuth == null){
            throw new Exception("unauthorized");
        }
        dataAccess.deleteAuth(authToken);
        // remove from GameData?
    }

    public GameData createGame(String authToken, String gameName) throws Exception {
        AuthData auth= dataAccess.getAuth(authToken);
        if (auth == null){
            throw new Exception("401: Bad Request");
        }

        if (gameName == null || gameName.isBlank()){
            throw new Exception("400: missing game name");
        }

        var username = auth.username();
        return dataAccess.createGame(username, gameName);
    }
}
