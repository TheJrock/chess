package dataaccess;

import datamodel.*;
import java.sql.*;
import java.util.HashMap;

public class MysqlDataAccess implements DataAccess {

    public MysqlDataAccess() {
        try {
            DatabaseManager.createDatabase();
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to configure database: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("SQL Error while configuring database", e);
        }
    }

    private void configureDatabase() throws SQLException, DataAccessException {
//        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {

            // USERS
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(255) PRIMARY KEY,
                    email VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL
                );
            """);

            // AUTH
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    FOREIGN KEY (username) REFERENCES user(username)
                        ON DELETE CASCADE
                );
            """);

            // GAMES
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS game (
                    gameID INT AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    FOREIGN KEY (whiteUsername) REFERENCES user(username)
                        ON DELETE SET NULL,
                    FOREIGN KEY (blackUsername) REFERENCES user(username)
                        ON DELETE SET NULL
                );
            """);
        }
    }

    // --- USER OPERATIONS ---

    @Override
    public void createUser(UserData user) {
        String statement = "INSERT INTO user (username, email, password) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, user.username());
            ps.setString(2, user.email());
            ps.setString(3, user.password());
            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                // We’ll let UserService interpret this as "Username Unavailable"
                throw new RuntimeException("Username Unavailable");
            }
            throw new RuntimeException("Database error while creating user", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public UserData getUser(String username) {
        String statement = "SELECT username, email, password FROM user WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while getting user", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    // --- AUTH OPERATIONS ---

    @Override
    public void createAuth(AuthData authData) {
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, authData.authToken());
            ps.setString(2, authData.username());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating auth", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) {
        String statement = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while getting auth", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) {
        String statement = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, authToken);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while deleting auth", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    // --- GAME OPERATIONS ---

    @Override
    public int createGame(GameData gameData) {
        String statement = "INSERT INTO game (whiteUsername, blackUsername, gameName) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, gameData.whiteUsername());
            ps.setString(2, gameData.blackUsername());
            ps.setString(3, gameData.gameName());
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating game", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public GameData getGame(int gameID) {
        String statement = "SELECT * FROM game WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName")
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while getting game", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public void updateGame(GameData gameData) {
        String statement = """
            UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ? WHERE gameID = ?
        """;
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, gameData.whiteUsername());
            ps.setString(2, gameData.blackUsername());
            ps.setString(3, gameData.gameName());
            ps.setInt(4, gameData.gameID());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Game not found");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while updating game", e);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public HashMap<Integer, GameData> getGames() {
        var games = new HashMap<Integer, GameData>();
        String statement = "SELECT * FROM game";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                var game = new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName")
                );
                games.put(game.gameID(), game);
            }
            return games;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while listing games", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM auth");
            stmt.executeUpdate("DELETE FROM game");
            stmt.executeUpdate("DELETE FROM user");

        } catch (SQLException e) {
            throw new RuntimeException("Database error while clearing tables", e);
//        } catch (DataAccessException e) {
//            throw new RuntimeException("Failed to connect to database", e);
        }
    }
}
