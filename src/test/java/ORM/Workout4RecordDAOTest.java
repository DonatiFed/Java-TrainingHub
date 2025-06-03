package Tests;

import ORM.DatabaseManager;
import ORM.Workout4RecordDAO;
import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.ExerciseIntensitySetter;
import Model.WorkoutManagement.Workout4Record;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Workout4RecordDAOTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;
    private Workout4RecordDAO workout4RecordDAO;
    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockStatement = mock(Statement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseManager = Mockito.mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        workout4RecordDAO = Mockito.spy(new Workout4RecordDAO());

        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
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
    void testAddWorkout4Record() throws SQLException {
        String date = "2025-05-22";
        ResultSet generatedKeys = mock(ResultSet.class);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(42);

        Workout4Record record = workout4RecordDAO.addWorkout4Record(date);

        assertNotNull(record);
        assertEquals(42, record.getId());
        assertEquals(date, record.getDate());
    }

    @Test
    void testGetAllWorkout4Records() throws SQLException {
        // Simulate two rows in the ResultSet, then end
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("w4r_id")).thenReturn(1, 2);
        when(mockResultSet.getString("date")).thenReturn("2025-05-22", "2025-05-23");

        // Ensure createStatement and query execution return expected mocks
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SELECT * FROM Workout4Record")).thenReturn(mockResultSet);

        // Stub internal call to avoid real DB work
        doReturn(Collections.emptyList()).when(workout4RecordDAO).getExercisesForWorkout4Record(anyInt());

        // Run method under test
        List<Workout4Record> records = workout4RecordDAO.getAllWorkout4Records();

        // Assertions
        assertNotNull(records);
        assertEquals(2, records.size());

        assertEquals(1, records.get(0).getId());
        assertEquals("2025-05-22", records.get(0).getDate());

        assertEquals(2, records.get(1).getId());
        assertEquals("2025-05-23", records.get(1).getDate());

        // Verify ResultSet navigation
        verify(mockResultSet, times(3)).next();
        verify(mockResultSet, times(2)).getInt("w4r_id");
        verify(mockResultSet, times(2)).getString("date");
    }


    @Test
    void testGetWorkout4RecordById() throws SQLException {
        int id = 5;
        String date = "2025-01-01";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDate("date")).thenReturn(java.sql.Date.valueOf(date));

        doReturn(Collections.emptyList()).when(workout4RecordDAO).getExercisesForWorkout4Record(id);

        Workout4Record record = workout4RecordDAO.getWorkout4RecordById(id);

        assertNotNull(record);
        assertEquals(id, record.getId());
        assertEquals(date, record.getDate());
    }

    @Test
    void testUpdateWorkout4RecordDate() throws SQLException {
        int id = 5;
        String newDate = "2025-12-31";

        workout4RecordDAO.updateWorkout4RecordDate(id, newDate);

        verify(mockPreparedStatement).setDate(1, java.sql.Date.valueOf(newDate));
        verify(mockPreparedStatement).setInt(2, id);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDeleteWorkout4Record() throws SQLException {
        int id = 5;

        workout4RecordDAO.deleteWorkout4Record(id);

        verify(mockPreparedStatement).setInt(1, id);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testAddExerciseToWorkout4Record_WhenNotExists() throws SQLException {
        int w4rId = 10;
        int exId = 20;

        ResultSet checkResult = mock(ResultSet.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(contains("SELECT COUNT"))).thenReturn(checkStmt);
        when(checkStmt.executeQuery()).thenReturn(checkResult);
        when(checkResult.next()).thenReturn(true);
        when(checkResult.getInt(1)).thenReturn(0); // does not exist

        when(mockConnection.prepareStatement(contains("INSERT INTO Workout4Record_Exercises"))).thenReturn(insertStmt);

        workout4RecordDAO.addExerciseToWorkout4Record(w4rId, exId);

        verify(insertStmt).setInt(1, w4rId);
        verify(insertStmt).setInt(2, exId);
        verify(insertStmt).executeUpdate();
    }

    @Test
    void testGetExercisesForWorkout4Record() throws SQLException {
        int w4rId = 1;

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("ex_id")).thenReturn(10);
        when(mockResultSet.getString("exercise_name")).thenReturn("Push Up");
        when(mockResultSet.getString("exercise_description")).thenReturn("Chest workout");
        when(mockResultSet.getString("exercise_equipment")).thenReturn("None");
        when(mockResultSet.getInt("exercise_N_sets")).thenReturn(3);
        when(mockResultSet.getInt("exercise_N_reps")).thenReturn(15);
        when(mockResultSet.getInt("exercise_weight")).thenReturn(0);
        when(mockResultSet.getString("exercise_strategy")).thenReturn("BASIC");

        // Optionally, mock factory if needed
        MockedStatic<Model.WorkoutManagement.ExerciseStrategyFactory> mockedFactory = Mockito.mockStatic(Model.WorkoutManagement.ExerciseStrategyFactory.class);
        ExerciseIntensitySetter mockStrategy = mock(ExerciseIntensitySetter.class);
        mockedFactory.when(() -> Model.WorkoutManagement.ExerciseStrategyFactory.createStrategy(anyString())).thenReturn(mockStrategy);

        List<Exercise> exercises = workout4RecordDAO.getExercisesForWorkout4Record(w4rId);

        assertEquals(1, exercises.size());
        assertEquals("Push Up", exercises.get(0).getName());

        mockedFactory.close();
    }
}
