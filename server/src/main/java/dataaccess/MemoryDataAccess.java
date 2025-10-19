package dataaccess;

import datamodel.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }
}
