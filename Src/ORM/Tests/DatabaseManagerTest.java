package ORM.Tests;

import ORM.DatabaseManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DatabaseManagerTest {

    private MockedStatic<DriverManager> mockedDriverManager;
    private Connection mockConnection;

    @BeforeEach
    void setUp() throws SQLException {
        // Mock the Connection object
        mockConnection = mock(Connection.class);

        // Mock DriverManager's getConnection method
        mockedDriverManager = Mockito.mockStatic(DriverManager.class);
        mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);

        // Mock Connection.close() to do nothing when called
        doNothing().when(mockConnection).close();

        // Call getConnection here to avoid it being printed multiple times
        DatabaseManager.getConnection();
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Close the connection mock
        DatabaseManager.closeConnection();

        // Close the mocked static DriverManager
        mockedDriverManager.close();
    }

    @Test
    void testGetConnection() throws SQLException {
        // Act
        Connection connection = DatabaseManager.getConnection();

        // Assert
        assertNotNull(connection, "Connection should not be null");
        assertEquals(mockConnection, connection, "Returned connection should be the mock connection");
    }

    @Test
    void testCloseConnection() throws SQLException {
        // Act
        DatabaseManager.getConnection();  // Initialize connection
        DatabaseManager.closeConnection(); // Close connection

        // Assert
        verify(mockConnection, times(1)).close(); // Verify close() was called once
    }
}


