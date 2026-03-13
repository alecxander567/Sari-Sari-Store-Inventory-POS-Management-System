package main;
import config.DatabaseConnection;
import java.sql.Connection;

public class Main {

	public static void main(String[] args) {
		Connection conn = DatabaseConnection.getConnection();

	   if (conn != null) {
	        System.out.println("Connection Successful!");
	   }
	}
}
