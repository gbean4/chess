package dataaccess;

import datamodel.UserData;

public interface DataAccess {
    void clear();
    void addUser(UserData user);
    UserData getUser (String username);

}
