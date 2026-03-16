package dao;

import config.DatabaseConnection;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class userDAO {

    private static final int BCRYPT_ROUNDS = 12;

    public User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                boolean passwordMatches;

                if (isHashed(storedPassword)) {
                    passwordMatches = BCrypt.checkpw(password, storedPassword);
                } else {
                    passwordMatches = storedPassword.equals(password);
                    if (passwordMatches) {
                        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
                        rehashPassword(rs.getInt("user_id"), hashed, conn);
                        storedPassword = hashed; 
                    }
                }

                if (passwordMatches) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(storedPassword);
                    user.setFullName(rs.getString("full_name"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    try { user.setPhoneNumber(rs.getString("phone_number")); }
                    catch (SQLException ignored) {}
                    int storeId = rs.getInt("store_id");
                    user.setStoreId(rs.wasNull() ? null : storeId);
                    return user;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createUserWithStore(User user, String storeName) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement storeStmt = conn.prepareStatement(
                "INSERT INTO store (store_name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS);
            storeStmt.setString(1, storeName);
            storeStmt.executeUpdate();
            ResultSet rs = storeStmt.getGeneratedKeys();
            int storeId = rs.next() ? rs.getInt(1) : 0;

            String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(BCRYPT_ROUNDS));

            PreparedStatement userStmt = conn.prepareStatement(
                "INSERT INTO users (username, password, full_name, store_id) VALUES (?, ?, ?, ?)");
            userStmt.setString(1, user.getUsername());
            userStmt.setString(2, hashed);
            userStmt.setString(3, user.getFullName());
            userStmt.setInt(4, storeId);
            return userStmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getStoreName(Integer storeId) {
        if (storeId == null) return null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT store_name FROM store WHERE store_id = ?")) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("store_name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Returns true if the string looks like a BCrypt hash. */
    private boolean isHashed(String password) {
        return password != null && password.startsWith("$2a$");
    }

    /** Saves a newly hashed password for a user (called during auto-rehash). */
    private void rehashPassword(int userId, String hashed, Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET password = ? WHERE user_id = ?")) {
            stmt.setString(1, hashed);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Hash a plain-text password. Use this everywhere a password is saved. */
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /** Verify a plain-text password against a stored hash. */
    public static boolean verifyPassword(String plainText, String hashed) {
        if (hashed == null || !hashed.startsWith("$2a$")) return false;
        return BCrypt.checkpw(plainText, hashed);
    }
}