package config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    public static Connection getConnection() {
        Connection connection = null;

        try {
            Properties props = new Properties();
            try (InputStream in = new FileInputStream("config.properties")) {
                props.load(in);
            }

            String host = props.getProperty("db.host", "localhost");
            String port = props.getProperty("db.port", "3306");
            String dbName = props.getProperty("db.name", "sari_sari_store_db");
            String user = props.getProperty("db.user", "root");
            String password = props.getProperty("db.password", "");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

            // Connect
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database Connected Successfully!");

        } catch (SQLException e) {
            System.out.println("Database Connection Failed!");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error loading config.properties");
            e.printStackTrace();
        }

        return connection;
    }
}