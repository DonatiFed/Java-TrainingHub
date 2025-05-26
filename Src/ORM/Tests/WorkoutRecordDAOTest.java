package ORM.Tests;

import Model.WorkoutManagement.WorkoutRecord;
import Model.WorkoutManagement.Workout4Record;
import ORM.DatabaseManager;
import ORM.WorkoutRecordDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkoutRecordDAOTest {

    private Connection mockConnection;
    private Statement mockStatement;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private WorkoutRecordDAO workoutRecordDAO;
    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseManager = Mockito.mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        workoutRecordDAO = new WorkoutRecordDAO();

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        if (mockedDatabaseManager != null) {
            mockedDatabaseManager.close();
        }
    }

    @Test
    void testAddWorkoutRecord() throws SQLException {
        String sql = "INSERT INTO WorkoutRecords (last_edit_date, N_workouts) VALUES (?, 0)";
        int generatedId = 5;
        LocalDate currentDate = LocalDate.now();

        // Mock ResultSet for generated keys
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        // Mock behaviors
        when(mockConnection.prepareStatement(eq(sql), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(generatedId);

        // Call method
        workoutRecordDAO = new WorkoutRecordDAO(mockConnection);
        WorkoutRecord result = workoutRecordDAO.addWorkoutRecord();

        // Verify interactions
        verify(mockConnection).prepareStatement(eq(sql), eq(Statement.RETURN_GENERATED_KEYS));
        verify(mockPreparedStatement).setDate(1, java.sql.Date.valueOf(currentDate));
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys();
        verify(mockGeneratedKeys).next();
        verify(mockGeneratedKeys).getInt(1);

        // Assert result
        assertNotNull(result);
        assertEquals(generatedId, result.getId());
    }

    @Test
    void testAddWorkoutRecord_NoRowsAffected() throws SQLException {
        String sql = "INSERT INTO WorkoutRecords (last_edit_date, N_workouts) VALUES (?, 0)";
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        when(mockConnection.prepareStatement(eq(sql), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // No rows affected
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false); // No generated keys

        workoutRecordDAO = new WorkoutRecordDAO(mockConnection);
        WorkoutRecord result = workoutRecordDAO.addWorkoutRecord();

        assertNull(result);
        verify(mockConnection).prepareStatement(eq(sql), eq(Statement.RETURN_GENERATED_KEYS));
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys();
    }

    @Test
    void testAddWorkoutRecord_NoGeneratedKeys() throws SQLException {
        String sql = "INSERT INTO WorkoutRecords (last_edit_date, N_workouts) VALUES (?, 0)";
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        when(mockConnection.prepareStatement(eq(sql), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false); // No generated keys

        workoutRecordDAO = new WorkoutRecordDAO(mockConnection);
        WorkoutRecord result = workoutRecordDAO.addWorkoutRecord();

        assertNull(result);
        verify(mockConnection).prepareStatement(eq(sql), eq(Statement.RETURN_GENERATED_KEYS));
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys();
        verify(mockGeneratedKeys).next();
    }

    @Test
    void testLinkWorkoutRecordToUser() throws SQLException {
        int userId = 1;
        int wrId = 2;
        String sql = "INSERT INTO WorkoutRecords_AppUser (user_id, wr_id) VALUES (?, ?)";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);

        workoutRecordDAO = new WorkoutRecordDAO(mockConnection);
        workoutRecordDAO.linkWorkoutRecordToUser(userId, wrId);

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).setInt(2, wrId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testGetWorkoutRecordById() throws SQLException {
        int wrId = 1;
        String sql = "SELECT * FROM WorkoutRecords WHERE wr_id = ?";

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(java.sql.Date.valueOf("2023-10-27"));
        when(mockResultSet.getInt("N_workouts")).thenReturn(5);

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        WorkoutRecordDAO spyDAO = Mockito.spy(workoutRecordDAO);
        doReturn(Collections.emptyList()).when(spyDAO).getWorkout4RecordsByWorkoutRecordId(wrId);

        WorkoutRecord record = spyDAO.getWorkoutRecordById(wrId);

        assertNotNull(record);
        assertEquals(wrId, record.getId());
        assertEquals("2023-10-27", record.getLastEditDate());
        assertEquals(5, record.getnWorkouts());

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wrId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(spyDAO).getWorkout4RecordsByWorkoutRecordId(wrId);
    }

    @Test
    void testGetWorkoutRecordById_NotFound() throws SQLException {
        int wrId = 1;
        String sql = "SELECT * FROM WorkoutRecords WHERE wr_id = ?";

        when(mockResultSet.next()).thenReturn(false);
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        WorkoutRecord record = workoutRecordDAO.getWorkoutRecordById(wrId);

        assertNull(record);
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wrId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
    }

    @Test
    void testGetAllWorkoutRecords() throws SQLException {
        String sql = "SELECT * FROM WorkoutRecords";

        // Mock ResultSet behavior: 2 rows then end
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("wr_id")).thenReturn(1, 2);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(
                java.sql.Date.valueOf("2023-10-27"),
                java.sql.Date.valueOf("2023-10-28")
        );
        when(mockResultSet.getInt("N_workouts")).thenReturn(3, 7);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(sql)).thenReturn(mockResultSet);

        // Create spy to stub internal method
        WorkoutRecordDAO spyDAO = Mockito.spy(workoutRecordDAO);
        doReturn(Collections.emptyList()).when(spyDAO).getWorkout4RecordsByWorkoutRecordId(anyInt());

        List<WorkoutRecord> records = spyDAO.getAllWorkoutRecords();

        assertNotNull(records);
        assertEquals(2, records.size());

        assertEquals(1, records.get(0).getId());
        assertEquals("2023-10-27", records.get(0).getLastEditDate());
        assertEquals(3, records.get(0).getnWorkouts());

        assertEquals(2, records.get(1).getId());
        assertEquals("2023-10-28", records.get(1).getLastEditDate());
        assertEquals(7, records.get(1).getnWorkouts());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(sql);
        verify(mockResultSet, times(3)).next();
        verify(spyDAO, times(2)).getWorkout4RecordsByWorkoutRecordId(anyInt());
    }

    @Test
    void testDeleteWorkoutRecord() throws SQLException {
        int wrId = 1;
        String sql = "DELETE FROM WorkoutRecords WHERE wr_id = ?";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);

        workoutRecordDAO.deleteWorkoutRecord(wrId);

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wrId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testGetWorkout4RecordsByWorkoutRecordId() throws SQLException {
        int wrId = 1;
        String sql = "SELECT w4r.w4r_id, w4r.date FROM Workout4Record w4r JOIN Workout4Record_WorkoutRecords wrr ON w4r.w4r_id = wrr.w4r_id WHERE wrr.wr_id = ?";

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("w4r_id")).thenReturn(101, 102);
        when(mockResultSet.getString("date")).thenReturn("2023-10-27", "2023-10-28");

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Workout4Record> workout4Records = workoutRecordDAO.getWorkout4RecordsByWorkoutRecordId(wrId);

        assertNotNull(workout4Records);
        assertEquals(2, workout4Records.size());

        assertEquals(101, workout4Records.get(0).getId());
        assertEquals("2023-10-27", workout4Records.get(0).getDate());

        assertEquals(102, workout4Records.get(1).getId());
        assertEquals("2023-10-28", workout4Records.get(1).getDate());

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wrId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next();
    }

    @Test
    void testAddWorkout4RecordToWorkoutRecord() throws SQLException {
        int wrId = 1;
        int w4rId = 101;

        String checkSql = "SELECT COUNT(*) FROM Workout4Record_WorkoutRecords WHERE w4r_id = ? AND wr_id = ?";
        String insertSql = "INSERT INTO Workout4Record_WorkoutRecords (w4r_id, wr_id) VALUES (?, ?)";

        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);

        // Mock check for existing relationship (return false - no existing relationship)
        when(mockConnection.prepareStatement(checkSql)).thenReturn(mockCheckStmt);
        when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
        when(mockCheckRs.next()).thenReturn(true);
        when(mockCheckRs.getInt(1)).thenReturn(0); // No existing relationship

        // Mock insert
        when(mockConnection.prepareStatement(insertSql)).thenReturn(mockInsertStmt);

        workoutRecordDAO = new WorkoutRecordDAO(mockConnection);
        workoutRecordDAO.addWorkout4RecordToWorkoutRecord(wrId, w4rId);

        verify(mockConnection).prepareStatement(checkSql);
        verify(mockCheckStmt).setInt(1, w4rId);
        verify(mockCheckStmt).setInt(2, wrId);
        verify(mockCheckStmt).executeQuery();
        verify(mockCheckRs).next();
        verify(mockCheckRs).getInt(1);

        verify(mockConnection).prepareStatement(insertSql);
        verify(mockInsertStmt).setInt(1, w4rId);
        verify(mockInsertStmt).setInt(2, wrId);
        verify(mockInsertStmt).executeUpdate();
    }

    @Test
    void testAddWorkout4RecordToWorkoutRecord_AlreadyLinked() throws SQLException {
        int wrId = 1;
        int w4rId = 101;

        String checkSql = "SELECT COUNT(*) FROM Workout4Record_WorkoutRecords WHERE w4r_id = ? AND wr_id = ?";

        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);

        // Mock check for existing relationship (return true - relationship already exists)
        when(mockConnection.prepareStatement(checkSql)).thenReturn(mockCheckStmt);
        when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
        when(mockCheckRs.next()).thenReturn(true);
        when(mockCheckRs.getInt(1)).thenReturn(1); // Existing relationship found

        workoutRecordDAO = new WorkoutRecordDAO(mockConnection);
        workoutRecordDAO.addWorkout4RecordToWorkoutRecord(wrId, w4rId);

        verify(mockConnection).prepareStatement(checkSql);
        verify(mockCheckStmt).setInt(1, w4rId);
        verify(mockCheckStmt).setInt(2, wrId);
        verify(mockCheckStmt).executeQuery();
        verify(mockCheckRs).next();
        verify(mockCheckRs).getInt(1);

        // Verify that insert is NOT called since relationship already exists
        verify(mockConnection, never()).prepareStatement("INSERT INTO Workout4Record_WorkoutRecords (w4r_id, wr_id) VALUES (?, ?)");
    }

    @Test
    void testGetWorkoutRecordByUserId_Found() throws SQLException {
        int userId = 1;
        int wrId = 10;
        String sql = "SELECT wr.* FROM WorkoutRecords wr " +
                "JOIN workoutrecords_appuser wau ON wr.wr_id = wau.wr_id " +
                "WHERE wau.user_id = ?";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Mock ResultSet to return a valid WorkoutRecord row
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("wr_id")).thenReturn(wrId);
        // Add more getters here if your WorkoutRecord constructor requires more fields

        WorkoutRecordDAO dao = new WorkoutRecordDAO(mockConnection);
        WorkoutRecord record = dao.getWorkoutRecordByUserId(userId);

        assertNotNull(record);
        assertEquals(wrId, record.getId());

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getInt("wr_id");
    }

    @Test
    void testGetWorkoutRecordByUserId_NotFound() throws SQLException {
        int userId = 1;
        String sql = "SELECT wr.* FROM WorkoutRecords wr " +
                "JOIN workoutrecords_appuser wau ON wr.wr_id = wau.wr_id " +
                "WHERE wau.user_id = ?";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Mock ResultSet to return no rows
        when(mockResultSet.next()).thenReturn(false);

        WorkoutRecordDAO dao = new WorkoutRecordDAO(mockConnection);
        WorkoutRecord record = dao.getWorkoutRecordByUserId(userId);

        assertNull(record);

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
    }

}