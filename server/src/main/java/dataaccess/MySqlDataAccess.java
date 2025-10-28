package dataaccess;

import chess.ChessGame;
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
    public UserData getUser(String username) throws ResponseException {
        var statement = "SELECT username, email, password FROM UserData WHERE username = ?";
        try(Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(statement)){
            ps.setString(1, username);
            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    String uname = rs.getString("username");
                    String email = rs.getString("email");
                    String password = rs.getString("password");
                    return new UserData(uname, email, password);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("unable to connect to user: %s", e.getMessage()));
        } catch (DataAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws ResponseException {
        var statement = "SELECT authToken, username FROM AuthData WHERE authToken = ?";
        try(Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(statement)){
            ps.setString(1, authToken);
            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    String authT = rs.getString("authToken");
                    String username = rs.getString("username");
                    return new AuthData(authT, username);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("unable to connect to user: %s", e.getMessage()));
        } catch (DataAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws ResponseException {
        var statement = "DELETE FROM AuthData WHERE authToken = ?; ";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)){
            ps.setString(1, authToken);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected ==0){
                throw new ResponseException(ResponseException.Code.BadRequest, "authToken not found");
            }
        } catch (SQLException e) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("unable to connect to user: %s", e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createAuth(AuthData auth) {
        var statement ="INSERT INTO AuthData (username, authToken) VALUES(?, ?, ?)";
        try {
            executeUpdate(statement, auth.username(), auth.authToken());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int createGame(String gameName) {
        var statement ="INSERT INTO GameData (whiteUsername, blackUsername, gameName, game) VALUES(?, ?, ?, ?)";
        try {
            ChessGame game = new ChessGame();
            String gameJson = new Gson().toJson(game);

            return executeUpdate(statement,null, null, gameName, gameJson);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameData[] listGames(String authToken) {
        return new GameData[0];
    }

    @Override
    public void joinGame(String username, GameSpec gameSpec) {

    }

    @Override
    public GameData getGame(int gameID) throws ResponseException {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM GameData WHERE gameID = ?";
        try(Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(statement)){
            ps.setInt(1, gameID);
            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    int foundGameID = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String gameJson = rs.getString("game");
                    Gson gson = new Gson();
                    ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                    return new GameData(foundGameID, whiteUsername,blackUsername, gameName, game);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("unable to connect to user: %s", e.getMessage()));
        } catch (DataAccessException e){
            throw new RuntimeException(e);
        }
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
              whiteUsername VARCHAR(255),
              blackUsername VARCHAR(255),
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
