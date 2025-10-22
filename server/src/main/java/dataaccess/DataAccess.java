package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.GameSpec;
import datamodel.UserData;

public interface DataAccess {
    void clear();
    void createUser(UserData user);
    UserData getUser (String username);

    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);
    void createAuth(AuthData auth);

    GameData createGame(String gameName);
    GameData [] listGames(String authToken);
    void joinGame(String username, GameSpec gameSpec);

    GameData getGame(int gameID);
}
