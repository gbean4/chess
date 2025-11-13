package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.GameSpec;
import datamodel.UserData;
import exception.DataAccessException;
import exception.ResponseException;

public interface DataAccess {
    void clear();
    void createUser(UserData user);
    UserData getUser (String username) throws ResponseException;

    AuthData getAuth(String authToken) throws ResponseException;
    void deleteAuth(String authToken) throws ResponseException;
    void createAuth(AuthData auth);

    int createGame(String gameName);
    GameData [] listGames(String authToken) throws ResponseException;
    void joinGame(String username, GameSpec gameSpec) throws ResponseException;

    GameData getGame(int gameID) throws ResponseException;
    void leaveGame(String username, int gameID) throws DataAccessException;
    void resignGame(String username, int gameID);

    void updateGame(GameData game);
}
