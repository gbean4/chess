package dataaccess;

import com.google.gson.Gson;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.GameSpec;
import datamodel.UserData;
import exception.DataAccessException;
import exception.ResponseException;

import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {
    public MySqlDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() {
        var statement = "TRUNCATE TABLE UserData; TRUNCATE TABLE gameData; TRUNCATE TABLE AuthData";
        try {
            executeUpdate(statement);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createUser(UserData user) {
        var statement ="INSERT INTO UserData (username, email, password) VALUES(?, ?, ?)";
        try {
            executeUpdate(statement, user.username(), user.email(), user.password());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) {

    }

    @Override
    public void createAuth(AuthData auth) {

    }

    @Override
    public GameData createGame(String gameName) {
        return null;
    }

    @Override
    public GameData[] listGames(String authToken) {
        return new GameData[0];
    }

    @Override
    public void joinGame(String username, GameSpec gameSpec) {

    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public void updateGame(GameData game) {

    }

    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);
                        case null -> ps.setNull(i + 1, NULL);
                        default -> {
                        }
                    }
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    rs.getInt(1);
                }

            }
        } catch (SQLException e) {
            throw new ResponseException(
                    ResponseException.Code.ServerError,
                    String.format("unable to update database: %s, %s", statement, e.getMessage()));
        } catch (DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("unable to connect to database: %s", e.getMessage()));
        }
        return 0;
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  UserData (
              username VARCHAR(50) NOT NULL,
              email VARCHAR(255) NOT NULL,
              password VARCHAR(255) NOT NULL,
              PRIMARY KEY (username),
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """ ,
            """
            CREATE TABLE IF NOT EXISTS  GameData (
              gameID INT NOT NULL AUTO_INCREMENT,
              whiteUSERNAME VARCHAR(255),
              blackUSERNAME VARCHAR(255),
              gameName VARCHAR(255),
              game JSON,
              PRIMARY KEY (gameID),
              FOREIGN KEY (whiteUsername) REFERENCES UserData(username) ON DELETE SET NULL ON UPDATE CASCADE,
              FOREIGN KEY (blackUsername) REFERENCES UserData(username) ON DELETE SET NULL ON UPDATE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS  AuthData (
              authToken VARCHAR(255) NOT NULL,
              username VARCHAR(255) NOT NULL,
              PRIMARY KEY (authToken),
              FOREIGN KEY (username) REFERENCES UserData(username) ON DELETE SET NULL ON UPDATE CASCADE,
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
