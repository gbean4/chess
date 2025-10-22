package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.*;

import java.util.Objects;
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
        return dataAccess.createGame(gameName);
    }

    public GameData[] listGames(String authToken) throws Exception {
        AuthData auth= dataAccess.getAuth(authToken);
        if (auth == null){
            throw new Exception("401: Bad Request");
        }
        var existingAuth= dataAccess.getAuth(authToken);
        if (existingAuth == null){
            throw new Exception("unauthorized");
        }

        return dataAccess.listGames(authToken);
    }

    public void joinGame(String authToken, GameSpec gameSpec) throws Exception {
        AuthData auth= dataAccess.getAuth(authToken);
        if (auth == null){
            throw new Exception("401: Bad Request");
        }
        var existingAuth= dataAccess.getAuth(authToken);
        if (existingAuth == null){
            throw new Exception("unauthorized");
        }

        String color = gameSpec.playerColor();
        if (color == null || (!color.equalsIgnoreCase("white") && (!color.equalsIgnoreCase("black")))){
            throw new Exception("400: invalid color");
        }

        var username = auth.username();
        var game = dataAccess.getGame(gameSpec.gameID());

        if (game.whiteUsername() != null && game.blackUsername() != null){
            throw new Exception("already taken");
        } else if ((Objects.equals(gameSpec.playerColor().toLowerCase(), "white") && game.whiteUsername()!= null)){
            throw new Exception("already taken");
        } else if ((Objects.equals(gameSpec.playerColor().toLowerCase(), "black") && game.blackUsername()!= null)){
            throw new Exception("already taken");
        } else {
            dataAccess.joinGame(username, gameSpec);}
    }
}
