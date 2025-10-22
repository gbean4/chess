package dataaccess;

import chess.ChessGame;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();

    private final AtomicInteger gameIDCounter = new AtomicInteger();
    public int nextID(){
        return gameIDCounter.incrementAndGet();
    }

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }
    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }
    @Override
    public void createAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
//        return auth;
    }

    @Override
    public GameData createGame(String username, String gameName) {
        int gameID = nextID();
        GameData gameData = new GameData(gameID, username, null, gameName, new ChessGame());
        games.put(gameID, gameData);
        return gameData;
    }

    public void listAuth() {
        for (Map.Entry<String, AuthData> entry: authTokens.entrySet()){
            System.out.println("Token: " + entry.getKey() + ", User: "+ entry.getValue());
        }
    }
}
