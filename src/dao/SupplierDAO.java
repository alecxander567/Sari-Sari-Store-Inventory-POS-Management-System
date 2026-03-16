package dao;

import model.Supplier;
import config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public boolean addSupplier(Supplier supplier, int storeId) {
        String sql = """
                INSERT INTO supplier
                (supplier_name, contact_number, email, address, store_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactNumber());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getAddress());
            stmt.setInt(5, storeId);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Supplier> getAllSuppliers(int storeId) {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM supplier WHERE store_id = ? ORDER BY supplier_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Supplier s = new Supplier();
                s.setSupplierId(rs.getInt("supplier_id"));
                s.setSupplierName(rs.getString("supplier_name"));
                s.setContactNumber(rs.getString("contact_number"));
                s.setEmail(rs.getString("email"));
                s.setAddress(rs.getString("address"));
                suppliers.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public boolean updateSupplier(Supplier supplier, int storeId) {
        String sql = """
                UPDATE supplier
                SET supplier_name = ?,
                    contact_number = ?,
                    email = ?,
                    address = ?
                WHERE supplier_id = ? AND store_id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactNumber());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getAddress());
            stmt.setInt(5, supplier.getSupplierId());
            stmt.setInt(6, storeId);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteSupplier(int supplierId, int storeId) {
        String sql = "DELETE FROM supplier WHERE supplier_id = ? AND store_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            stmt.setInt(2, storeId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}