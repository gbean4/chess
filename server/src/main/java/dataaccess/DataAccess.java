package dataaccess;

import datamodel.AuthData;
import datamodel.UserData;

public interface DataAccess {
    void clear();
    void createUser(UserData user);
    UserData getUser (String username);

    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);
    void createAuth(AuthData auth);
}
