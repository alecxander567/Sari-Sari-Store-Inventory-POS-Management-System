package dao;

import config.DatabaseConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class userDAO {

    public User login(String username, String password) {

        User user = null;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setCreatedAt(rs.getTimestamp("created_at"));

                int storeId = rs.getInt("store_id");
                if (rs.wasNull()) {
                    user.setStoreId(null);
                } else {
                    user.setStoreId(storeId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public String getStoreName(Integer storeId) {
        if (storeId == null) return null;

        String query = "SELECT store_name FROM store WHERE store_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("store_name");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean createUserWithStore(User user, String storeName) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            String storeQuery = "INSERT INTO store (store_name) VALUES (?)";
            PreparedStatement storeStmt = conn.prepareStatement(storeQuery, Statement.RETURN_GENERATED_KEYS);
            storeStmt.setString(1, storeName);
            storeStmt.executeUpdate();

            ResultSet rs = storeStmt.getGeneratedKeys();
            int storeId = 0;
            if (rs.next()) {
                storeId = rs.getInt(1);
            }

            String userQuery = "INSERT INTO users (username, password, full_name, store_id) VALUES (?, ?, ?, ?)";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, user.getUsername());
            userStmt.setString(2, user.getPassword());
            userStmt.setString(3, user.getFullName());
            userStmt.setInt(4, storeId);

            int rows = userStmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}