package ORM.Tests;

import Model.WorkoutManagement.ExerciseIntensitySetter;
import ORM.DatabaseManager;
import ORM.WorkoutPlanDAO;
import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.Workout4Plan;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkoutPlanDAOTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;
    private WorkoutPlanDAO dao;
    private MockedStatic<DatabaseManager> mockedDB;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);

        mockedDB = Mockito.mockStatic(DatabaseManager.class);
        mockedDB.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        dao = spy(new WorkoutPlanDAO());

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
    void testAddWorkoutPlan() throws SQLException {
        ResultSet generatedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(42);

        WorkoutPlan result = dao.addWorkoutPlan();

        assertNotNull(result);
        assertEquals(42, result.getId());
    }

    @Test
    void testGetAllWorkoutPlans() throws SQLException {
        // Setup ResultSet for WorkoutPlans
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("wp_id")).thenReturn(1, 2);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(java.sql.Date.valueOf("2025-01-01"), java.sql.Date.valueOf("2025-01-02"));

        // Stub Statement's executeQuery to return mocked ResultSet
        doReturn(mockResultSet).when(mockStatement).executeQuery("SELECT * FROM WorkoutPlans");

        // Stub getWorkout4PlansByWorkoutPlanId to return empty lists
        doReturn(Collections.emptyList()).when(dao).getWorkout4PlansByWorkoutPlanId(anyInt());

        List<WorkoutPlan> plans = dao.getAllWorkoutPlans();

        assertEquals(2, plans.size());
        assertEquals(1, plans.get(0).getId());
        assertEquals("2025-01-01", plans.get(0).getLastEditDate());
        assertEquals(2, plans.get(1).getId());
        assertEquals("2025-01-02", plans.get(1).getLastEditDate());
    }

    @Test
    void testGetWorkoutPlanById() throws SQLException {
        int wpId = 5;

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(java.sql.Date.valueOf("2025-05-05"));

        doReturn(Collections.emptyList()).when(dao).getWorkout4PlansByWorkoutPlanId(wpId);

        WorkoutPlan plan = dao.getWorkoutPlanById(wpId);

        assertNotNull(plan);
        assertEquals(wpId, plan.getId());
        assertEquals("2025-05-05", plan.getLastEditDate());
    }

    @Test
    void testDeleteWorkoutPlan() throws SQLException {
        dao.deleteWorkoutPlan(10);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testGetWorkout4PlansByWorkoutPlanId() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("w4p_id")).thenReturn(101, 102);
        when(rs.getString("day")).thenReturn("Monday", "Tuesday");
        when(rs.getString("strategy")).thenReturn("Strength", "Endurance");

        List<Workout4Plan> plans = dao.getWorkout4PlansByWorkoutPlanId(1);

        assertEquals(2, plans.size());
        assertEquals("Monday", plans.get(0).getDay());
        assertInstanceOf(ExerciseIntensitySetter.class, plans.get(0).getStrategy());
        assertEquals(101, plans.get(0).getId());

        assertEquals("Tuesday", plans.get(1).getDay());
        assertInstanceOf(ExerciseIntensitySetter.class, plans.get(1).getStrategy());
        assertEquals(102, plans.get(1).getId());
    }

    @Test
    void testAddWorkout4PlanToWorkoutPlan_successfulInsert() throws SQLException {
        // Mock existence checks for wpId and w4pId
        PreparedStatement checkExistsStmt = mock(PreparedStatement.class);
        ResultSet rsExists = mock(ResultSet.class);

        // First call for wpId existence
        when(mockConnection.prepareStatement(startsWith("SELECT COUNT(*) FROM WorkoutPlans WHERE wp_id ="))).thenReturn(checkExistsStmt);
        when(checkExistsStmt.executeQuery()).thenReturn(rsExists);
        when(rsExists.next()).thenReturn(true);
        when(rsExists.getInt(1)).thenReturn(1);

        // Second call for w4pId existence
        PreparedStatement checkExistsStmt2 = mock(PreparedStatement.class);
        ResultSet rsExists2 = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT COUNT(*) FROM Workout4Plan WHERE w4p_id ="))).thenReturn(checkExistsStmt2);
        when(checkExistsStmt2.executeQuery()).thenReturn(rsExists2);
        when(rsExists2.next()).thenReturn(true);
        when(rsExists2.getInt(1)).thenReturn(1);

        // Check if link exists in relationship table
        PreparedStatement checkLinkStmt = mock(PreparedStatement.class);
        ResultSet rsLink = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT COUNT(*) FROM WorkoutPlans_Workout4Plans"))).thenReturn(checkLinkStmt);
        when(checkLinkStmt.executeQuery()).thenReturn(rsLink);
        when(rsLink.next()).thenReturn(true);
        when(rsLink.getInt(1)).thenReturn(0); // link does not exist

        // Insert statement
        PreparedStatement insertStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("INSERT INTO WorkoutPlans_Workout4Plans"))).thenReturn(insertStmt);

        dao.addWorkout4PlanToWorkoutPlan(1, 2);

        verify(insertStmt).executeUpdate();
    }

    @Test
    void testAddWorkout4PlanToWorkoutPlan_alreadyLinked() throws SQLException {
        // Mock existence checks for wpId and w4pId
        PreparedStatement checkExistsStmt = mock(PreparedStatement.class);
        ResultSet rsExists = mock(ResultSet.class);

        when(mockConnection.prepareStatement(startsWith("SELECT COUNT(*) FROM WorkoutPlans WHERE wp_id ="))).thenReturn(checkExistsStmt);
        when(checkExistsStmt.executeQuery()).thenReturn(rsExists);
        when(rsExists.next()).thenReturn(true);
        when(rsExists.getInt(1)).thenReturn(1);

        PreparedStatement checkExistsStmt2 = mock(PreparedStatement.class);
        ResultSet rsExists2 = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT COUNT(*) FROM Workout4Plan WHERE w4p_id ="))).thenReturn(checkExistsStmt2);
        when(checkExistsStmt2.executeQuery()).thenReturn(rsExists2);
        when(rsExists2.next()).thenReturn(true);
        when(rsExists2.getInt(1)).thenReturn(1);

        PreparedStatement checkLinkStmt = mock(PreparedStatement.class);
        ResultSet rsLink = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT COUNT(*) FROM WorkoutPlans_Workout4Plans"))).thenReturn(checkLinkStmt);
        when(checkLinkStmt.executeQuery()).thenReturn(rsLink);
        when(rsLink.next()).thenReturn(true);
        when(rsLink.getInt(1)).thenReturn(1); // link exists

        dao.addWorkout4PlanToWorkoutPlan(1, 2);

        verify(mockConnection, never()).prepareStatement(startsWith("INSERT INTO WorkoutPlans_Workout4Plans"));
    }

}