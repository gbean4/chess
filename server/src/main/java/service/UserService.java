package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.RegisterResponse;
import datamodel.UserData;

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
        return new RegisterResponse(user, user.username(),  "zyz");
    }
}
