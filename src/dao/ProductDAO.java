package dao;

import config.DatabaseConnection;
import model.InventoryLog;
import model.Product;

import java.sql.*;
import java.util.*;

public class ProductDAO {
	
	private InventoryLogDAO logDAO = new InventoryLogDAO();

    public List<Product> getAllProducts(int storeId) {
        List<Product> list = new ArrayList<>();
        String sql =
            "SELECT p.product_id, p.product_name, p.price, p.stock_quantity," +
            " p.created_by, p.created_at, p.category_id, p.supplier_id," +
            " c.category_name, s.supplier_name" +
            " FROM products p" +
            " LEFT JOIN categories c ON p.category_id = c.category_id" +
            " LEFT JOIN supplier   s ON p.supplier_id = s.supplier_id" +
            " WHERE p.store_id = ?" +
            " ORDER BY p.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> searchProducts(String keyword, int storeId) {
        List<Product> list = new ArrayList<>();
        String kw = "%" + keyword + "%";
        String sql =
            "SELECT p.product_id, p.product_name, p.price, p.stock_quantity," +
            " p.created_by, p.created_at, p.category_id, p.supplier_id," +
            " c.category_name, s.supplier_name" +
            " FROM products p" +
            " LEFT JOIN categories c ON p.category_id = c.category_id" +
            " LEFT JOIN supplier   s ON p.supplier_id = s.supplier_id" +
            " WHERE p.store_id = ?" +
            "   AND (p.product_name LIKE ? OR c.category_name LIKE ? OR s.supplier_name LIKE ?)" +
            " ORDER BY p.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, storeId);
            stmt.setString(2, kw); stmt.setString(3, kw); stmt.setString(4, kw);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean addProduct(Product p, int createdByUserId, int storeId) {
        String sql =
            "INSERT INTO products (product_name, price, stock_quantity, store_id, created_by, category_id, supplier_id)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { 
            stmt.setString(1, p.getProductName());
            stmt.setBigDecimal(2, p.getPrice());
            stmt.setInt(3, p.getStockQuantity());
            stmt.setInt(4, storeId);
            stmt.setInt(5, createdByUserId);
            setNullableInt(stmt, 6, p.getCategoryId());
            setNullableInt(stmt, 7, p.getSupplierId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int productId = keys.getInt(1);

                    InventoryLog log = new InventoryLog();
                    log.setProductId(productId);
                    log.setUserId(createdByUserId);
                    log.setOperation("ADD");
                    log.setQuantityChanged(p.getStockQuantity());
                    logDAO.addLog(log);
                }
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateProduct(Product p, int userId) {
        String sql =
            "UPDATE products SET product_name=?, price=?, stock_quantity=?, category_id=?, supplier_id=?" +
            " WHERE product_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int oldStock = 0;
            String selectSql = "SELECT stock_quantity FROM products WHERE product_id = ?";
            try (PreparedStatement sel = conn.prepareStatement(selectSql)) {
                sel.setInt(1, p.getProductId());
                ResultSet rs = sel.executeQuery();
                if (rs.next()) oldStock = rs.getInt("stock_quantity");
            }

            stmt.setString(1, p.getProductName());
            stmt.setBigDecimal(2, p.getPrice());
            stmt.setInt(3, p.getStockQuantity());
            setNullableInt(stmt, 4, p.getCategoryId());
            setNullableInt(stmt, 5, p.getSupplierId());
            stmt.setInt(6, p.getProductId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                int diff = p.getStockQuantity() - oldStock; 

                InventoryLog log = new InventoryLog();
                log.setProductId(p.getProductId());
                log.setUserId(userId);
                log.setOperation("UPDATE");
                log.setQuantityChanged(diff); 
                logDAO.addLog(log);

                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Saves a snapshot of the product into disposed_items, then hard-deletes
     * the product row. Both run in a single transaction.
     */
    public boolean disposeProduct(Product product, String reason, int storeId, int userId) {
        String insertSql =
            "INSERT INTO disposed_items" +
            " (product_id, product_name, price, category_name, supplier_name," +
            "  quantity, reason, store_id, created_by)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String deleteSql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ins = conn.prepareStatement(insertSql);
                 PreparedStatement del = conn.prepareStatement(deleteSql)) {

                ins.setInt(1,        product.getProductId());
                ins.setString(2,     product.getProductName());
                ins.setBigDecimal(3, product.getPrice());
                ins.setString(4,     product.getCategoryName());
                ins.setString(5,     product.getSupplierName());
                ins.setInt(6,        product.getStockQuantity());
                ins.setString(7,     reason);
                ins.setInt(8,        storeId);
                ins.setObject(9,     userId, Types.INTEGER);
                ins.executeUpdate();

                InventoryLog log = new InventoryLog();
                log.setProductId(product.getProductId());
                log.setUserId(userId);
                log.setOperation("DISPOSE");
                log.setQuantityChanged(product.getStockQuantity());
                new InventoryLogDAO().addLog(log);

                del.setInt(1, product.getProductId());
                del.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteProduct(int productId, int userId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                InventoryLog log = new InventoryLog();
                log.setProductId(productId);
                log.setUserId(userId);
                log.setOperation("DELETE");
                log.setQuantityChanged(0);
                logDAO.addLog(log);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public Map<Integer, String> getCategoryMap() {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT category_id, category_name FROM categories ORDER BY category_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) map.put(rs.getInt(1), rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    public Map<Integer, String> getSupplierMap() {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT supplier_id, supplier_name FROM supplier ORDER BY supplier_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) map.put(rs.getInt(1), rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        int catId = rs.getInt("category_id");
        p.setCategoryId(rs.wasNull() ? null : catId);
        int supId = rs.getInt("supplier_id");
        p.setSupplierId(rs.wasNull() ? null : supId);
        p.setCategoryName(rs.getString("category_name"));
        p.setSupplierName(rs.getString("supplier_name"));
        return p;
    }

    private void setNullableInt(PreparedStatement s, int i, Integer v) throws SQLException {
        if (v != null) s.setInt(i, v); else s.setNull(i, Types.INTEGER);
    }
}