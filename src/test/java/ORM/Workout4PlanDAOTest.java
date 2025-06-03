package Tests;


import ORM.DatabaseManager;
import ORM.Workout4PlanDAO;
import Model.WorkoutManagement.Workout4Plan;
import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.ExerciseIntensitySetter;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Workout4PlanDAOTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;
    private Workout4PlanDAO dao;
    private MockedStatic<DatabaseManager> mockedDB;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);

        mockedDB = Mockito.mockStatic(DatabaseManager.class);
        mockedDB.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        dao = spy(new Workout4PlanDAO());

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
    void testAddWorkout4Plan() throws SQLException {
        String day = "Monday", strategy = "Endurance";
        ResultSet generatedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(101);

        Workout4Plan result = dao.addWorkout4Plan(day, strategy);

        assertNotNull(result);
        assertEquals(101, result.getId());
        assertEquals(day, result.getDay());
        assertInstanceOf(ExerciseIntensitySetter.class, result.getStrategy());
    }

    @Test
    void testGetAllWorkout4Plans() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("w4p_id")).thenReturn(1, 2);
        when(mockResultSet.getString("day")).thenReturn("Mon", "Tue");
        when(mockResultSet.getString("strategy")).thenReturn("Endurance", "Strength");

        doReturn(mockResultSet).when(mockStatement).executeQuery("SELECT * FROM Workout4Plan");
        doReturn(Collections.emptyList()).when(dao).getExercisesForWorkout4Plan(anyInt());

        List<Workout4Plan> list = dao.getAllWorkout4Plans();

        assertEquals(2, list.size());
        assertEquals("Mon", list.get(0).getDay());
        assertEquals("Tue", list.get(1).getDay());
    }

    @Test
    void testGetWorkout4PlanById() throws SQLException {
        int id = 5;
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("day")).thenReturn("Friday");
        when(mockResultSet.getString("strategy")).thenReturn("Strength");

        doReturn(Collections.emptyList()).when(dao).getExercisesForWorkout4Plan(id);

        Workout4Plan result = dao.getWorkout4PlanById(id);

        assertNotNull(result);
        assertEquals("Friday", result.getDay());
        assertInstanceOf(ExerciseIntensitySetter.class, result.getStrategy());
        assertEquals(id, result.getId());
    }

    @Test
    void testUpdateWorkout4Plan() throws SQLException {
        dao.updateWorkout4Plan(10, "Wed", "Hard");
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testDeleteWorkout4Plan() throws SQLException {
        dao.deleteWorkout4Plan(3);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testAddExerciseToWorkout4Plan_whenNewLink() throws SQLException {
        ResultSet rsCheck = mock(ResultSet.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(checkStmt);
        when(mockConnection.prepareStatement(startsWith("INSERT"))).thenReturn(insertStmt);
        when(checkStmt.executeQuery()).thenReturn(rsCheck);
        when(rsCheck.next()).thenReturn(true);
        when(rsCheck.getInt(1)).thenReturn(0); // not linked yet

        dao.addExerciseToWorkout4Plan(1, 2);
        verify(insertStmt).executeUpdate();
    }

    @Test
    void testAddExerciseToWorkout4Plan_whenAlreadyLinked() throws SQLException {
        ResultSet rsCheck = mock(ResultSet.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(checkStmt);
        when(checkStmt.executeQuery()).thenReturn(rsCheck);
        when(rsCheck.next()).thenReturn(true);
        when(rsCheck.getInt(1)).thenReturn(1); // already linked

        dao.addExerciseToWorkout4Plan(1, 2);
        verify(mockConnection, never()).prepareStatement(startsWith("INSERT"));
    }

    @Test
    void testGetExercisesForWorkout4Plan() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("ex_id")).thenReturn(10);
        when(rs.getString("exercise_name")).thenReturn("Push-up");
        when(rs.getString("exercise_description")).thenReturn("Do a push-up");
        when(rs.getString("exercise_equipment")).thenReturn("None");
        when(rs.getInt("exercise_N_sets")).thenReturn(3);
        when(rs.getInt("exercise_N_reps")).thenReturn(15);
        when(rs.getInt("exercise_weight")).thenReturn(0);
        when(rs.getString("exercise_strategy")).thenReturn("Endurance");

        List<Exercise> exercises = dao.getExercisesForWorkout4Plan(1);
        assertEquals(1, exercises.size());
        assertEquals("Push-up", exercises.get(0).getName());
    }
}
