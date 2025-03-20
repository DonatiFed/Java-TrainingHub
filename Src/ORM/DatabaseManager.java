package ORM;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = System.getenv("DATABASE_PASSWORD"); //TODO: Look if it is added to env variables for the application running
    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private DatabaseManager() {}

    // Singleton instance getter
    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println(" Connected to the database!");
            } catch (SQLException e) {
                System.err.println(" Database connection failed!");
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Close connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println(" Database connection closed.");
            } catch (SQLException e) {
                System.err.println(" Failed to close the database connection.");
                e.printStackTrace();
            }
        }
    }
}
