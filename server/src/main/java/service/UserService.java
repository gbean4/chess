package service;

import dataaccess.DataAccess;
import datamodel.*;
import exception.ResponseException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    String makeUserPassword(String clearTextPassword) {
        return BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
    }

    boolean verifyUser(String username, String providedClearTextPassword) {
        // read the previously hashed password from the database
        var hashedPassword = readHashedPasswordFromDatabase(username);

        return BCrypt.checkpw(providedClearTextPassword, hashedPassword);
    }

    private String readHashedPasswordFromDatabase(String username) {
        UserData userData;
        try {
            userData = dataAccess.getUser(username);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
        return userData.password();
    }

    public RegisterResponse register(UserData user) throws Exception {
        if (user.username()==null || user.password()==null ||user.email()==null){
            throw new Exception("Missing required fields");
        }
        var existingUser = dataAccess.getUser(user.username());
        if (existingUser != null){
            throw new ResponseException(ResponseException.Code.Forbidden, "User already exists");
        }

        String hashedPassword = makeUserPassword(user.password());
        dataAccess.createUser(new UserData(user.username(), user.email(), hashedPassword));
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
        if (!verifyUser(user.username(), password)){
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

    public int createGame(String authToken, String gameName) throws Exception {
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

    public GameData joinGame(String authToken, GameSpec gameSpec) throws Exception {
        AuthData auth= dataAccess.getAuth(authToken);
        if (auth == null){
            throw new Exception("401: unauthorized");
        }

        var username = auth.username();
        var game = dataAccess.getGame(gameSpec.gameID());
        if (game == null){
            throw new ResponseException(ResponseException.Code.BadRequest, "400: bad request - game not found");
        }

        String color = gameSpec.playerColor();
        if (color == null || color.isBlank()){
            throw new Exception(("400: bad request - invalid color"));
        }

        color = color.toLowerCase();
        if (!Objects.equals(color, "white") && !Objects.equals(color, "black")){
            throw new Exception(("400: bad request - invalid color"));
        }


        if (game.whiteUsername() != null && game.blackUsername() != null){
            throw new Exception("403 already taken");
        } else if ((Objects.equals(color.toLowerCase(), "white") && game.whiteUsername()!= null)){
            throw new Exception("403 already taken");
        } else if ((Objects.equals(color.toLowerCase(), "black") && game.blackUsername()!= null)){
            throw new Exception("403 already taken");
        } else {
            dataAccess.joinGame(username, gameSpec);
            return dataAccess.getGame(gameSpec.gameID());
        }
    }
    public void leaveGame(String authToken, GameSpec gameSpec) throws Exception{
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth== null) {
            throw new Exception("401 Unauthorized");
        }

        dataAccess.leaveGame(auth.username(), gameSpec.gameID());
    }

    public void resignGame(String authToken, GameSpec gameSpec) throws Exception{
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth== null) {
            throw new Exception("401 Unauthorized");
        }

        dataAccess.resignGame(auth.username(), gameSpec.gameID());
    }

    public Object getGame(String authToken, int gameID) throws Exception {
        AuthData auth= dataAccess.getAuth(authToken);
        if (auth == null){
            throw new Exception("401: unauthorized");
        }
        return dataAccess.getGame(gameID);
    }
}
