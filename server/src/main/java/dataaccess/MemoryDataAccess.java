package dataaccess;

import chess.ChessGame;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.GameSpec;
import datamodel.UserData;

import java.util.*;
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
        games.clear();
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
    }

@Override
public int createGame(String gameName) {
    int gameID = nextID();
    GameData gameData = new GameData(gameID, null, null, gameName, new ChessGame());
    games.put(gameID, gameData);
    return gameData.gameID();
}

    @Override
    public GameData[] listGames(String authToken) {

        List<GameData> gameList = new ArrayList<>();

        for (Map.Entry<Integer, GameData> entry: games.entrySet()){
            GameData g = entry.getValue();
            gameList.add(new GameData(entry.getKey(),
                    g.whiteUsername(),
                    g.blackUsername(),
                    g.gameName(),
                    g.game()));
        }
        return gameList.toArray(new GameData[0]);
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }


    @Override
    public void joinGame(String username, GameSpec gameSpec) {
        var game = games.get(gameSpec.gameID());
        if (Objects.equals(gameSpec.playerColor().toLowerCase(), "white")){
            games.put(game.gameID(),new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game()));
        } else{
            games.put(game.gameID(),new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game()));
        }
    }
}
