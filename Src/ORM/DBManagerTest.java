package ORM;

import java.sql.Connection;

public class DBManagerTest {
    public static void main(String[] args) {
        // Test database connection
        Connection conn = DatabaseManager.getConnection();

        if (conn != null) {
            System.out.println(" Connection successful!");
        } else {
            System.out.println(" Connection failed.");
        }

        // Close connection after testing
        DatabaseManager.closeConnection();
    }
}