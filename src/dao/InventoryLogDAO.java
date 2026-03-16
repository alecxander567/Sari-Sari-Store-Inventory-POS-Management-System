package dao;

import model.InventoryLog;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InventoryLogDAO {

    public void addLog(InventoryLog log) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO inventory_logs(product_id, user_id, action_type, change_quantity, created_at) VALUES(?,?,?,?,NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (log.getProductId() == 0) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, log.getProductId());
            }
            stmt.setInt(2, log.getUserId());
            stmt.setString(3, log.getOperation());
            stmt.setInt(4, log.getQuantityChanged());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<InventoryLog> getAllLogs() {
        List<InventoryLog> logs = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT il.*, p.product_name, u.username FROM inventory_logs il " +
                    "LEFT JOIN products p ON il.product_id = p.product_id " +
                    "JOIN users u ON il.user_id = u.user_id " +
                    "ORDER BY il.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    /** Fetches only logs belonging to the given user — used by InventoryLogPanel. */
    public List<InventoryLog> getLogsByUser(int userId) {
        List<InventoryLog> logs = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT il.*, p.product_name, u.username FROM inventory_logs il " +
                    "LEFT JOIN products p ON il.product_id = p.product_id " +
                    "JOIN users u ON il.user_id = u.user_id " +
                    "WHERE il.user_id = ? " +
                    "ORDER BY il.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    /** Shared row-mapping helper to avoid code duplication. */
    private InventoryLog mapRow(ResultSet rs) throws Exception {
        InventoryLog log = new InventoryLog();
        log.setLogId(rs.getInt("log_id"));
        log.setProductId(rs.getInt("product_id"));
        String productName = rs.getString("product_name");
        log.setProductName(productName != null ? productName : "Disposed Product");
        log.setUsername(rs.getString("username"));
        log.setOperation(rs.getString("action_type"));
        log.setQuantityChanged(rs.getInt("change_quantity"));
        log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return log;
    }
}