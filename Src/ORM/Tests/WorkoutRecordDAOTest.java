package ORM.Tests;

import ORM.DatabaseManager;
import ORM.WorkoutRecordDAO;
import Model.WorkoutManagement.WorkoutRecord;
import Model.WorkoutManagement.Workout4Record;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkoutRecordDAOTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;
    private WorkoutRecordDAO dao;
    private MockedStatic<DatabaseManager> mockedDB;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);

        mockedDB = Mockito.mockStatic(DatabaseManager.class);
        mockedDB.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        dao = spy(new WorkoutRecordDAO());

        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        mockedDB.close();
    }

    @Test
    void testAddWorkoutRecord() throws SQLException {
        ResultSet generatedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(42);

        WorkoutRecord result = dao.addWorkoutRecord();

        assertNotNull(result);
        assertEquals(42, result.getId());
    }

    @Test
    void testGetAllWorkoutRecords() throws SQLException {
        // Mocking ResultSet to simulate two records
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("wr_id")).thenReturn(1, 2);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(
                java.sql.Date.valueOf("2025-05-20"),
                java.sql.Date.valueOf("2025-05-21")
        );
        when(mockResultSet.getInt("N_workouts")).thenReturn(3, 5);

        doReturn(Collections.emptyList()).when(dao).getWorkout4RecordsByWorkoutRecordId(anyInt());
        doReturn(mockResultSet).when(mockStatement).executeQuery("SELECT * FROM WorkoutRecords");

        List<WorkoutRecord> records = dao.getAllWorkoutRecords();

        assertEquals(2, records.size());
        assertEquals(1, records.get(0).getId());
        assertEquals("2025-05-20", records.get(0).getLastEditDate());
        assertEquals(3, records.get(0).getnWorkouts());
    }

    @Test
    void testGetWorkoutRecordById() throws SQLException {
        int id = 10;
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(java.sql.Date.valueOf("2025-05-22"));
        when(mockResultSet.getInt("N_workouts")).thenReturn(7);

        doReturn(Collections.emptyList()).when(dao).getWorkout4RecordsByWorkoutRecordId(id);

        WorkoutRecord record = dao.getWorkoutRecordById(id);

        assertNotNull(record);
        assertEquals(id, record.getId());
        assertEquals("2025-05-22", record.getLastEditDate());
        assertEquals(7, record.getnWorkouts());
    }

    @Test
    void testDeleteWorkoutRecord() throws SQLException {
        dao.deleteWorkoutRecord(5);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testGetWorkout4RecordsByWorkoutRecordId() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("w4r_id")).thenReturn(100, 101);
        when(rs.getString("date")).thenReturn("2025-05-01", "2025-05-02");

        List<Workout4Record> w4Records = dao.getWorkout4RecordsByWorkoutRecordId(1);

        assertEquals(2, w4Records.size());
        assertEquals(100, w4Records.get(0).getId());
        assertEquals("2025-05-01", w4Records.get(0).getDate());
    }

    @Test
    void testAddWorkout4RecordToWorkoutRecord_whenNotLinked() throws SQLException {
        ResultSet rsCheck = mock(ResultSet.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(checkStmt);
        when(mockConnection.prepareStatement(startsWith("INSERT"))).thenReturn(insertStmt);
        when(checkStmt.executeQuery()).thenReturn(rsCheck);
        when(rsCheck.next()).thenReturn(true);
        when(rsCheck.getInt(1)).thenReturn(0); // Not linked yet

        dao.addWorkout4RecordToWorkoutRecord(1, 2);

        verify(insertStmt).executeUpdate();
    }

    @Test
    void testAddWorkout4RecordToWorkoutRecord_whenAlreadyLinked() throws SQLException {
        ResultSet rsCheck = mock(ResultSet.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(checkStmt);
        when(checkStmt.executeQuery()).thenReturn(rsCheck);
        when(rsCheck.next()).thenReturn(true);
        when(rsCheck.getInt(1)).thenReturn(1); // Already linked

        dao.addWorkout4RecordToWorkoutRecord(1, 2);

        verify(mockConnection, never()).prepareStatement(startsWith("INSERT"));
    }
}
