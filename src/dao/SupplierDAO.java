package dao;

import model.Supplier;
import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public boolean addSupplier(Supplier supplier) {

        String sql = """
                INSERT INTO supplier
                (supplier_name, contact_number, email, address)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactNumber());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getAddress());

            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public List<Supplier> getAllSuppliers(){

        List<Supplier> suppliers = new ArrayList<>();

        String sql = "SELECT * FROM supplier";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){

            while(rs.next()){

                Supplier s = new Supplier();

                s.setSupplierId(rs.getInt("supplier_id"));
                s.setSupplierName(rs.getString("supplier_name"));
                s.setContactNumber(rs.getString("contact_number"));
                s.setEmail(rs.getString("email"));
                s.setAddress(rs.getString("address"));

                suppliers.add(s);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return suppliers;
    }
    
    public boolean updateSupplier(Supplier supplier){

        String sql = """
                UPDATE supplier
                SET supplier_name = ?,
                    contact_number = ?,
                    email = ?,
                    address = ?
                WHERE supplier_id = ?
                """;

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactNumber());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getAddress());
            stmt.setInt(5, supplier.getSupplierId());

            stmt.executeUpdate();

            return true;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
    
    public boolean deleteSupplier(int supplierId){

        String sql = "DELETE FROM supplier WHERE supplier_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, supplierId);

            return stmt.executeUpdate() > 0;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}