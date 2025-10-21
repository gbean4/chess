package dataaccess;

import datamodel.AuthData;
import datamodel.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();

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
    public void listAuth() {
        for (Map.Entry<String, AuthData> entry: authTokens.entrySet()){
            System.out.println("Token: " + entry.getKey() + ", User: "+ entry.getValue());
        }
    }
}
