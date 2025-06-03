package Tests;

import Model.WorkoutManagement.ExerciseIntensitySetter;
import ORM.DatabaseManager;
import ORM.WorkoutPlanDAO;
import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.Workout4Plan;
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

class WorkoutPlanDAOTest {

    private Connection mockConnection;
    private Statement mockStatement;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private WorkoutPlanDAO workoutPlanDAO;
    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseManager = Mockito.mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        workoutPlanDAO = new WorkoutPlanDAO();

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
    void testAddWorkoutPlan() throws SQLException {
        String sql = "INSERT INTO WorkoutPlans (last_edit_date) VALUES (?)";
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
        workoutPlanDAO = new WorkoutPlanDAO(mockConnection);
        WorkoutPlan result = workoutPlanDAO.addWorkoutPlan();

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
    void testGetAllWorkoutPlans() throws SQLException {
        String sql = "SELECT * FROM WorkoutPlans";

        // Mock ResultSet behavior: 2 rows then end
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("wp_id")).thenReturn(1, 2);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(
                java.sql.Date.valueOf("2023-10-27"),
                java.sql.Date.valueOf("2023-10-28")
        );

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(sql)).thenReturn(mockResultSet);

        // Create spy to stub internal method
        WorkoutPlanDAO spyDAO = Mockito.spy(workoutPlanDAO);
        doReturn(Collections.emptyList()).when(spyDAO).getWorkout4PlansByWorkoutPlanId(anyInt());

        List<WorkoutPlan> plans = spyDAO.getAllWorkoutPlans();

        assertNotNull(plans);
        assertEquals(2, plans.size());

        assertEquals(1, plans.get(0).getId());
        assertEquals("2023-10-27", plans.get(0).getLastEditDate());

        assertEquals(2, plans.get(1).getId());
        assertEquals("2023-10-28", plans.get(1).getLastEditDate());

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(sql);
        verify(mockResultSet, times(3)).next();
        verify(spyDAO, times(2)).getWorkout4PlansByWorkoutPlanId(anyInt());
    }

    @Test
    void testGetWorkoutPlanById() throws SQLException {
        int wpId = 1;
        String sql = "SELECT * FROM WorkoutPlans WHERE wp_id = ?";

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(java.sql.Date.valueOf("2023-10-27"));

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        WorkoutPlanDAO spyDAO = Mockito.spy(workoutPlanDAO);
        doReturn(Collections.emptyList()).when(spyDAO).getWorkout4PlansByWorkoutPlanId(wpId);

        WorkoutPlan plan = spyDAO.getWorkoutPlanById(wpId);

        assertNotNull(plan);
        assertEquals(wpId, plan.getId());
        assertEquals("2023-10-27", plan.getLastEditDate());

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wpId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(spyDAO).getWorkout4PlansByWorkoutPlanId(wpId);
    }

    @Test
    void testGetWorkoutPlanById_NotFound() throws SQLException {
        int wpId = 1;
        String sql = "SELECT * FROM WorkoutPlans WHERE wp_id = ?";

        when(mockResultSet.next()).thenReturn(false);
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        WorkoutPlan plan = workoutPlanDAO.getWorkoutPlanById(wpId);

        assertNull(plan);
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wpId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
    }

    @Test
    void testUpdateLastEditDate() throws SQLException {
        int wpId = 1;
        String newDate = "2023-11-01";
        String sql = "UPDATE WorkoutPlans SET last_edit_date = ? WHERE wp_id = ?";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = workoutPlanDAO.updateLastEditDate(wpId, newDate);

        assertTrue(result);
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setDate(1, java.sql.Date.valueOf(newDate));
        verify(mockPreparedStatement).setInt(2, wpId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdateLastEditDate_NoRowsAffected() throws SQLException {
        int wpId = 1;
        String newDate = "2023-11-01";
        String sql = "UPDATE WorkoutPlans SET last_edit_date = ? WHERE wp_id = ?";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        boolean result = workoutPlanDAO.updateLastEditDate(wpId, newDate);

        assertFalse(result);
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDeleteWorkoutPlan() throws SQLException {
        int wpId = 1;
        String sql = "DELETE FROM WorkoutPlans WHERE wp_id = ?";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);

        workoutPlanDAO.deleteWorkoutPlan(wpId);

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wpId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testGetWorkout4PlansByWorkoutPlanId() throws SQLException {
        int wpId = 1;
        String sql = "SELECT w4p.w4p_id, w4p.day, w4p.strategy FROM Workout4Plan w4p " +
                "JOIN WorkoutPlans_Workout4Plans wpp ON w4p.w4p_id = wpp.w4p_id WHERE wpp.wp_id = ?";

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("w4p_id")).thenReturn(101, 102);
        when(mockResultSet.getString("day")).thenReturn("Monday", "Tuesday");
        when(mockResultSet.getString("strategy")).thenReturn("Strength", "Endurance");

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Workout4Plan> workout4Plans = workoutPlanDAO.getWorkout4PlansByWorkoutPlanId(wpId);

        assertNotNull(workout4Plans);
        assertEquals(2, workout4Plans.size());

        assertEquals(101, workout4Plans.get(0).getId());
        assertEquals("Monday", workout4Plans.get(0).getDay());
        assertInstanceOf(ExerciseIntensitySetter.class, workout4Plans.get(0).getStrategy());

        assertEquals(102, workout4Plans.get(1).getId());
        assertEquals("Tuesday", workout4Plans.get(1).getDay());
        assertInstanceOf(ExerciseIntensitySetter.class, workout4Plans.get(1).getStrategy());

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wpId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next();
    }

    @Test
    void testAddWorkout4PlanToWorkoutPlan() throws SQLException {
        int wpId = 1;
        int w4pId = 101;

        // Mock the ID existence checks
        PreparedStatement mockCheckStmt1 = mock(PreparedStatement.class);
        PreparedStatement mockCheckStmt2 = mock(PreparedStatement.class);
        PreparedStatement mockCheckStmt3 = mock(PreparedStatement.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);

        ResultSet mockCheckRs1 = mock(ResultSet.class);
        ResultSet mockCheckRs2 = mock(ResultSet.class);
        ResultSet mockCheckRs3 = mock(ResultSet.class);

        // Mock ID existence checks (return true for both IDs)
        when(mockConnection.prepareStatement("SELECT COUNT(*) FROM WorkoutPlans WHERE wp_id = ?")).thenReturn(mockCheckStmt1);
        when(mockConnection.prepareStatement("SELECT COUNT(*) FROM Workout4Plan WHERE w4p_id = ?")).thenReturn(mockCheckStmt2);
        when(mockCheckStmt1.executeQuery()).thenReturn(mockCheckRs1);
        when(mockCheckStmt2.executeQuery()).thenReturn(mockCheckRs2);
        when(mockCheckRs1.next()).thenReturn(true);
        when(mockCheckRs2.next()).thenReturn(true);
        when(mockCheckRs1.getInt(1)).thenReturn(1);
        when(mockCheckRs2.getInt(1)).thenReturn(1);

        // Mock relationship existence check (return false - no existing relationship)
        String checkSql = "SELECT COUNT(*) FROM WorkoutPlans_Workout4Plans WHERE wp_id = ? AND w4p_id = ?";
        when(mockConnection.prepareStatement(checkSql)).thenReturn(mockCheckStmt3);
        when(mockCheckStmt3.executeQuery()).thenReturn(mockCheckRs3);
        when(mockCheckRs3.next()).thenReturn(true);
        when(mockCheckRs3.getInt(1)).thenReturn(0);

        // Mock insert
        String insertSql = "INSERT INTO WorkoutPlans_Workout4Plans (wp_id, w4p_id) VALUES (?, ?)";
        when(mockConnection.prepareStatement(insertSql)).thenReturn(mockInsertStmt);

        workoutPlanDAO = new WorkoutPlanDAO(mockConnection);
        workoutPlanDAO.addWorkout4PlanToWorkoutPlan(wpId, w4pId);

        verify(mockConnection).prepareStatement(checkSql);
        verify(mockCheckStmt3).setInt(1, wpId);
        verify(mockCheckStmt3).setInt(2, w4pId);
        verify(mockConnection).prepareStatement(insertSql);
        verify(mockInsertStmt).setInt(1, wpId);
        verify(mockInsertStmt).setInt(2, w4pId);
        verify(mockInsertStmt).executeUpdate();
    }

    @Test
    void testRemoveWorkout4PlanFromWorkoutPlan() throws SQLException {
        int wpId = 1;
        int w4pId = 101;
        String sql = "DELETE FROM WorkoutPlans_Workout4Plans WHERE wp_id = ? AND w4p_id = ?";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);

        workoutPlanDAO.removeWorkout4PlanFromWorkoutPlan(wpId, w4pId);

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wpId);
        verify(mockPreparedStatement).setInt(2, w4pId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testAssignWorkoutPlanToUser() throws SQLException {
        int workoutPlanId = 1;
        int traineeId = 2;
        int personalTrainerId = 3;

        // Mock ID existence and PT checks
        PreparedStatement mockCheckStmt1 = mock(PreparedStatement.class);
        PreparedStatement mockCheckStmt2 = mock(PreparedStatement.class);
        PreparedStatement mockCheckStmt3 = mock(PreparedStatement.class);
        PreparedStatement mockCheckStmt4 = mock(PreparedStatement.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);

        ResultSet mockCheckRs1 = mock(ResultSet.class);
        ResultSet mockCheckRs2 = mock(ResultSet.class);
        ResultSet mockCheckRs3 = mock(ResultSet.class);
        ResultSet mockCheckRs4 = mock(ResultSet.class);

        // Mock all existence checks to return true
        when(mockConnection.prepareStatement("SELECT COUNT(*) FROM WorkoutPlans WHERE wp_id = ?")).thenReturn(mockCheckStmt1);
        when(mockConnection.prepareStatement("SELECT COUNT(*) FROM AppUser WHERE user_id = ?")).thenReturn(mockCheckStmt2, mockCheckStmt3);
        when(mockConnection.prepareStatement("SELECT is_pt FROM AppUser WHERE user_id = ?")).thenReturn(mockCheckStmt4);

        when(mockCheckStmt1.executeQuery()).thenReturn(mockCheckRs1);
        when(mockCheckStmt2.executeQuery()).thenReturn(mockCheckRs2);
        when(mockCheckStmt3.executeQuery()).thenReturn(mockCheckRs3);
        when(mockCheckStmt4.executeQuery()).thenReturn(mockCheckRs4);

        when(mockCheckRs1.next()).thenReturn(true);
        when(mockCheckRs2.next()).thenReturn(true);
        when(mockCheckRs3.next()).thenReturn(true);
        when(mockCheckRs4.next()).thenReturn(true);

        when(mockCheckRs1.getInt(1)).thenReturn(1);
        when(mockCheckRs2.getInt(1)).thenReturn(1);
        when(mockCheckRs3.getInt(1)).thenReturn(1);
        when(mockCheckRs4.getBoolean("is_pt")).thenReturn(true);

        String insertSql = "INSERT INTO WorkoutPlans_PersonalTrainer_AppUser (wp_id, trainee_id, pt_id) VALUES (?, ?, ?)";
        when(mockConnection.prepareStatement(insertSql)).thenReturn(mockInsertStmt);

        workoutPlanDAO = new WorkoutPlanDAO(mockConnection);
        workoutPlanDAO.assignWorkoutPlanToUser(workoutPlanId, traineeId, personalTrainerId);

        verify(mockConnection).prepareStatement(insertSql);
        verify(mockInsertStmt).setInt(1, workoutPlanId);
        verify(mockInsertStmt).setInt(2, traineeId);
        verify(mockInsertStmt).setInt(3, personalTrainerId);
        verify(mockInsertStmt).executeUpdate();
    }

    @Test
    void testUnassignWorkoutPlan() throws SQLException {
        int wpId = 1;
        int traineeId = 2;
        int ptId = 3;
        String sql = "DELETE FROM WorkoutPlans_PersonalTrainer_AppUser WHERE wp_id = ? AND trainee_id = ? AND pt_id = ?";

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);

        workoutPlanDAO.unassignWorkoutPlan(wpId, traineeId, ptId);

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, wpId);
        verify(mockPreparedStatement).setInt(2, traineeId);
        verify(mockPreparedStatement).setInt(3, ptId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testGetWorkoutPlansByTraineeId() throws SQLException {
        int traineeId = 2;
        String sql = "SELECT wp.* FROM WorkoutPlans wp " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpu ON wp.wp_id = wpu.wp_id " +
                "WHERE wpu.trainee_id = ?";

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("wp_id")).thenReturn(1, 2);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(
                java.sql.Date.valueOf("2023-10-27"),
                java.sql.Date.valueOf("2023-10-28")
        );

        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        WorkoutPlanDAO spyDAO = Mockito.spy(workoutPlanDAO);
        doReturn(Collections.emptyList()).when(spyDAO).getWorkout4PlansByWorkoutPlanId(anyInt());

        List<WorkoutPlan> plans = spyDAO.getWorkoutPlansByTraineeId(traineeId);

        assertNotNull(plans);
        assertEquals(2, plans.size());

        assertEquals(1, plans.get(0).getId());
        assertEquals("2023-10-27", plans.get(0).getLastEditDate());

        assertEquals(2, plans.get(1).getId());
        assertEquals("2023-10-28", plans.get(1).getLastEditDate());

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, traineeId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next();
        verify(spyDAO, times(2)).getWorkout4PlansByWorkoutPlanId(anyInt());
    }
}