package dao;

import config.DatabaseConnection;
import model.Disposeditem;

import java.sql.*;
import java.util.*;

public class Disposeditemdao {

    public List<Disposeditem> getAllDisposedItems(int storeId) {
        List<Disposeditem> list = new ArrayList<>();
        String sql =
            "SELECT disposed_id, product_id, product_name, price," +
            " category_name, supplier_name, quantity, reason, disposed_at" +
            " FROM disposed_items" +
            " WHERE store_id = ?" +
            " ORDER BY disposed_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Disposeditem> searchDisposedItems(String keyword, int storeId) {
        List<Disposeditem> list = new ArrayList<>();
        String kw = "%" + keyword + "%";
        String sql =
            "SELECT disposed_id, product_id, product_name, price," +
            " category_name, supplier_name, quantity, reason, disposed_at" +
            " FROM disposed_items" +
            " WHERE store_id = ?" +
            "   AND (product_name LIKE ? OR category_name LIKE ?" +
            "        OR supplier_name LIKE ? OR reason LIKE ?)" +
            " ORDER BY disposed_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, storeId);
            stmt.setString(2, kw); stmt.setString(3, kw);
            stmt.setString(4, kw); stmt.setString(5, kw);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private Disposeditem mapRow(ResultSet rs) throws SQLException {
        Disposeditem d = new Disposeditem();
        d.setDisposeId(rs.getInt("disposed_id"));       
        d.setProductId(rs.getInt("product_id"));
        d.setProductName(rs.getString("product_name"));
        d.setPrice(rs.getBigDecimal("price"));
        d.setCategoryName(rs.getString("category_name"));
        d.setSupplierName(rs.getString("supplier_name"));
        d.setQuantity(rs.getInt("quantity"));
        d.setReason(rs.getString("reason"));
        d.setDisposedAt(rs.getTimestamp("disposed_at"));
        return d;
    }

    /**
     * Restores a disposed item back into the products table, then removes
     * it from disposed_items. Both steps run in a single transaction.
     */
    public boolean restoreItem(int disposedId) {
        String selectSql =
            "SELECT product_id, product_name, price, category_name, supplier_name," +
            " quantity, store_id, created_by FROM disposed_items WHERE disposed_id = ?";
        // Look up category_id and supplier_id by name (best-effort; null if not found)
        String catSql = "SELECT category_id FROM categories WHERE category_name = ? LIMIT 1";
        String supSql = "SELECT supplier_id FROM supplier  WHERE supplier_name  = ? LIMIT 1";
        String insertSql =
            "INSERT INTO products (product_name, price, stock_quantity, created_by, category_id, supplier_id)" +
            " VALUES (?, ?, ?, ?, ?, ?)";
        String deleteSql = "DELETE FROM disposed_items WHERE disposed_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                java.math.BigDecimal price; String productName; int quantity;
                Integer createdBy; String categoryName; String supplierName;
                try (PreparedStatement sel = conn.prepareStatement(selectSql)) {
                    sel.setInt(1, disposedId);
                    ResultSet rs = sel.executeQuery();
                    if (!rs.next()) return false;
                    productName  = rs.getString("product_name");
                    price        = rs.getBigDecimal("price");
                    quantity     = rs.getInt("quantity");
                    int cb       = rs.getInt("created_by");
                    createdBy    = rs.wasNull() ? null : cb;
                    categoryName = rs.getString("category_name");
                    supplierName = rs.getString("supplier_name");
                }

                Integer categoryId = null;
                if (categoryName != null && !categoryName.equals("—")) {
                    try (PreparedStatement cs = conn.prepareStatement(catSql)) {
                        cs.setString(1, categoryName);
                        ResultSet rs = cs.executeQuery();
                        if (rs.next()) categoryId = rs.getInt(1);
                    }
                }

                Integer supplierId = null;
                if (supplierName != null && !supplierName.equals("—")) {
                    try (PreparedStatement ss = conn.prepareStatement(supSql)) {
                        ss.setString(1, supplierName);
                        ResultSet rs = ss.executeQuery();
                        if (rs.next()) supplierId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setString(1, productName);
                    ins.setBigDecimal(2, price);
                    ins.setInt(3, quantity);
                    if (createdBy != null) ins.setInt(4, createdBy);
                    else ins.setNull(4, java.sql.Types.INTEGER);
                    if (categoryId != null) ins.setInt(5, categoryId);
                    else ins.setNull(5, java.sql.Types.INTEGER);
                    if (supplierId != null) ins.setInt(6, supplierId);
                    else ins.setNull(6, java.sql.Types.INTEGER);
                    ins.executeUpdate();
                }

                try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                    del.setInt(1, disposedId);
                    del.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /** Permanently deletes a single disposal record (does NOT restore the product). */
    public boolean deleteDisposedItem(int disposedId) {
        String sql = "DELETE FROM disposed_items WHERE disposed_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, disposedId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /** Permanently deletes ALL disposal records for a store. */
    public boolean deleteAllDisposedItems(int storeId) {
        String sql = "DELETE FROM disposed_items WHERE store_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, storeId);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}