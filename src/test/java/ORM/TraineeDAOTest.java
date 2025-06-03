package Tests;

import ORM.DatabaseManager;
import ORM.TraineeDAO;
import ORM.WorkoutRecordDAO;
import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.WorkoutRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TraineeDAOTest {

    // Mocks needed across multiple tests or setup
    private Connection mockConnection;
    private Statement mockStatement;
    private ResultSet mockResultSet; // Generic ResultSet mock, configured per test
    private TraineeDAO traineeDAO;
    private MockedStatic<DatabaseManager> mockedDatabaseManager; // Keep track to close

    @BeforeEach
    void setUp() throws SQLException {
        // Mock core JDBC objects
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        mockResultSet = mock(ResultSet.class); // Reused mock, behavior set in each test

        // Mock static DatabaseManager.getConnection() to return our mock connection
        // Start mocking BEFORE instantiating TraineeDAO
        mockedDatabaseManager = Mockito.mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        // Instantiate the DAO under test - it will receive the mockConnection
        traineeDAO = new TraineeDAO();

        // Basic setup for statement execution returning the generic ResultSet mock
        // Individual tests might override or specify behavior for PreparedStatement mocks
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    }

    @AfterEach
    void tearDown() {
        // Close the static mock to avoid interference between tests
        if (mockedDatabaseManager != null) {
            mockedDatabaseManager.close();
        }
    }

    @Test
    void testAddTrainee() throws SQLException {
        String name = "John Smith";
        int age = 28;
        int generatedId = 42;

        String sqlUser = "INSERT INTO AppUser (user_name, user_age, is_pt) VALUES (?, ?, false)";

        // Mock PreparedStatement and ResultSet
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        // Mock behavior of executeUpdate and getGeneratedKeys
        when(mockStmt.executeUpdate()).thenReturn(1);
        when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);

        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(generatedId);

        // Mock connection to return mocked PreparedStatement
        when(mockConnection.prepareStatement(eq(sqlUser), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);

        // Instantiate DAO with mocked connection
        TraineeDAO traineeDAO = new TraineeDAO(mockConnection);

        // Call the method under test
        Trainee result = traineeDAO.addTrainee(name, age);

        // Verify interactions with the mocks
        verify(mockConnection).prepareStatement(eq(sqlUser), eq(Statement.RETURN_GENERATED_KEYS));
        verify(mockStmt).setString(1, name);
        verify(mockStmt).setInt(2, age);
        verify(mockStmt).executeUpdate();
        verify(mockStmt).getGeneratedKeys();
        verify(mockRs).next();

        // Assert returned Trainee object correctness
        assertNotNull(result);
        assertEquals(generatedId, result.getId());
        assertEquals(name, result.getName());
        assertEquals(age, result.getAge());
    }


    @Test
    void testGetAllTrainees() throws SQLException {
        System.out.println("--- Starting testGetAllTrainees ---");
        // This test uses createStatement, no PreparedStatement mocking needed here.
        // Setup ResultSet behavior
        when(mockResultSet.next()).thenReturn(true, true, false); // Two trainees, then end
        when(mockResultSet.getInt("user_id")).thenReturn(1, 2);
        when(mockResultSet.getString("user_name")).thenReturn("John Doe", "Jane Smith");
        when(mockResultSet.getInt("user_age")).thenReturn(30, 25);

        // Execute
        List<Trainee> trainees = traineeDAO.getAllTrainees();

        // Assertions
        assertNotNull(trainees);
        assertEquals(2, trainees.size());
        assertEquals(1, trainees.get(0).getId());
        assertEquals("John Doe", trainees.get(0).getName());
        assertEquals(30, trainees.get(0).getAge());
        assertEquals(2, trainees.get(1).getId());
        assertEquals("Jane Smith", trainees.get(1).getName());
        assertEquals(25, trainees.get(1).getAge());

        // Verifications
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM AppUser WHERE is_pt = false");
        verify(mockResultSet, times(3)).next(); // Called for each row + one for false
        verify(mockResultSet, times(2)).getInt("user_id");
        verify(mockResultSet, times(2)).getString("user_name");
        verify(mockResultSet, times(2)).getInt("user_age");


        System.out.println("--- Finished testGetAllTrainees ---");
    }

    @Test
    void testGetWorkoutPlanFromUserId() throws SQLException {
        System.out.println("--- Starting testGetWorkoutPlanFromUserId ---");
        int userId = 1;
        int expectedWpId = 10;

        // Specific mock for this test
        PreparedStatement mockGetPlanStmt = mock(PreparedStatement.class);
        String sql = "SELECT wp.wp_id, wp.last_edit_date " +
                "FROM WorkoutPlans wp " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON wp.wp_id = wpt.wp_id " +
                "WHERE wpt.trainee_id = ?";

        // Configure Mocks
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockGetPlanStmt);
        when(mockGetPlanStmt.executeQuery()).thenReturn(mockResultSet); // Use the shared ResultSet mock
        when(mockResultSet.next()).thenReturn(true, false); // One plan, then end
        when(mockResultSet.getInt("wp_id")).thenReturn(expectedWpId);

        // Execute
        List<WorkoutPlan> workoutPlans = traineeDAO.getWorkoutPlanFromUserId(userId);

        // Assertions
        assertNotNull(workoutPlans);
        assertEquals(1, workoutPlans.size());
        assertEquals(expectedWpId, workoutPlans.get(0).getId());

        // Verifications
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockGetPlanStmt).setInt(1, userId);
        verify(mockGetPlanStmt).executeQuery();
        verify(mockResultSet, times(2)).next();
        verify(mockResultSet).getInt("wp_id");

        System.out.println("--- Finished testGetWorkoutPlanFromUserId ---");
    }

    @Test
    void testGetPTForUserId() throws SQLException {
        System.out.println("--- Starting testGetPTForUserId ---");
        int traineeId = 1;
        int expectedPtId = 2; // This ID should correspond to the user_id of the PT
        String expectedPtName = "Trainer";
        int expectedPtAge = 40;

        // Specific mock for this test
        PreparedStatement mockGetPtStmt = mock(PreparedStatement.class);
        // Ensure the SQL selects the columns needed by the constructor (user_id, user_name, user_age)
        String sql = "SELECT pt.pt_id, a.user_name, a.user_age " +
                "FROM Personal_Trainer pt " +
                "JOIN AppUser a ON pt.pt_id = a.user_id " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON pt.pt_id = wpt.pt_id " +
                "WHERE wpt.trainee_id = ?";

        // Configure Mocks
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockGetPtStmt);
        when(mockGetPtStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Found one PT
        // Mock ALL columns needed by the DAO's logic and the PersonalTrainer constructor
        // The DAO's getPTForUserId creates a PersonalTrainer with user_id, user_name, user_age
        // The query selects pt.pt_id, a.user_name, a.user_age
        // We need to mock "user_id" which in this case comes from pt.pt_id according to the join condition in the DAO
        when(mockResultSet.getInt("user_id")).thenReturn(expectedPtId);
        when(mockResultSet.getString("user_name")).thenReturn(expectedPtName);
        when(mockResultSet.getInt("user_age")).thenReturn(expectedPtAge);
        // We also need to mock pt_id for the query to work correctly in the DAO
        when(mockResultSet.getInt("pt_id")).thenReturn(expectedPtId);


        // Execute
        PersonalTrainer pt = traineeDAO.getPTForUserId(traineeId);

        // Assertions
        assertNotNull(pt);
        // The PersonalTrainer is constructed with (rs.getInt("user_id"), ...)
        assertEquals(expectedPtId, pt.getId());
        assertEquals(expectedPtName, pt.getName());
        assertEquals(expectedPtAge, pt.getAge());

        // Verifications
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockGetPtStmt).setInt(1, traineeId);
        verify(mockGetPtStmt).executeQuery();
        verify(mockResultSet).next();
        // Verify that the columns used by the constructor were fetched
        verify(mockResultSet).getInt(eq("user_id"));
        verify(mockResultSet).getString(eq("user_name"));
        verify(mockResultSet).getInt(eq("user_age"));
        // While the query selects pt_id, the DAO constructor doesn't use it directly for the PersonalTrainer's ID.
        // However, mocking it ensures the query in the DAO works as expected.
        // We don't need to explicitly verify its retrieval if the constructor doesn't use it for the ID.
        // If other logic in the DAO used rs.getInt("pt_id"), we would verify it.

        System.out.println("--- Finished testGetPTForUserId ---");
    }


    @Test
    void testEditUser() throws SQLException {
        System.out.println("--- Starting testEditUser ---");
        int userId = 1;
        String newName = "Updated Name";
        int newAge = 35;

        // Specific mock for this test
        PreparedStatement mockEditStmt = mock(PreparedStatement.class);
        String sql = "UPDATE AppUser SET user_name = ?, user_age = ? WHERE user_id = ?";

        // Configure Mocks
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockEditStmt);
        when(mockEditStmt.executeUpdate()).thenReturn(1); // Assume 1 row updated

        // Execute
        traineeDAO.editUser(userId, newName, newAge);

        // Verifications
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockEditStmt).setString(1, newName);
        verify(mockEditStmt).setInt(2, newAge);
        verify(mockEditStmt).setInt(3, userId);
        verify(mockEditStmt).executeUpdate();

        System.out.println("--- Finished testEditUser ---");
    }

    @Test
    void testDeleteUser() throws SQLException {
        System.out.println("--- Starting testDeleteUser ---");
        int userId = 1;

        // Specific mock for this test
        PreparedStatement mockDeleteStmt = mock(PreparedStatement.class);
        String sql = "DELETE FROM AppUser WHERE user_id = ?";

        // Configure Mocks
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockDeleteStmt);
        when(mockDeleteStmt.executeUpdate()).thenReturn(1); // Assume 1 row deleted

        // Execute
        traineeDAO.deleteUser(userId);

        // Verifications
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockDeleteStmt).setInt(1, userId);
        verify(mockDeleteStmt).executeUpdate();

        System.out.println("--- Finished testDeleteUser ---");
    }

    // Example Test for when GetPTForUserId finds no PT
    @Test
    void testGetPTForUserId_NotFound() throws SQLException {
        System.out.println("--- Starting testGetPTForUserId_NotFound ---");
        int traineeId = 5;

        PreparedStatement mockGetPtStmt = mock(PreparedStatement.class);
        // Ensure the SQL matches exactly what's used in the corresponding testGetPTForUserId
        String sql = "SELECT pt.pt_id, a.user_name, a.user_age " +
                "FROM Personal_Trainer pt " +
                "JOIN AppUser a ON pt.pt_id = a.user_id " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON pt.pt_id = wpt.pt_id " +
                "WHERE wpt.trainee_id = ?";

        // Configure Mocks - ResultSet.next() returns false immediately
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockGetPtStmt);
        when(mockGetPtStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Execute
        PersonalTrainer pt = traineeDAO.getPTForUserId(traineeId);

        // Assertions
        assertNull(pt, "PersonalTrainer should be null when not found");

        // Verifications
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockGetPtStmt).setInt(1, traineeId);
        verify(mockGetPtStmt).executeQuery();
        verify(mockResultSet).next(); // Called once, returned false
        System.out.println("--- Finished testGetPTForUserId_NotFound ---");
    }

    @Test
    void testGetWorkoutRecordByUserId() throws SQLException {
        System.out.println("--- Starting testGetWorkoutRecordByUserId ---");
        int userId = 1;
        int expectedWorkoutRecordId = 5;
        Date expectedCreationDate = Date.valueOf("2025-05-25");

        // Specific mock for this test
        PreparedStatement mockGetRecordStmt = mock(PreparedStatement.class);
        String sql = "SELECT wr.wr_id, wr.last_edit_date " +
                "FROM WorkoutRecords wr " +
                "JOIN WorkoutRecords_AppUser wra ON wr.wr_id = wra.wr_id " +
                "WHERE wra.user_id = ?";

        // Configure Mocks
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockGetRecordStmt);
        when(mockGetRecordStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("wr_id")).thenReturn(expectedWorkoutRecordId);
        when(mockResultSet.getDate("last_edit_date")).thenReturn(expectedCreationDate);

        // Execute
        WorkoutRecord workoutRecord = traineeDAO.getWorkoutRecordByUserId(userId);

        // Assertions
        assertNotNull(workoutRecord);
        assertEquals(expectedWorkoutRecordId, workoutRecord.getId());
        // We are not currently using the creationDate in the WorkoutRecord constructor
        // If you do in the future, you'd want to assert it here.

        // Verifications
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockGetRecordStmt).setInt(1, userId);
        verify(mockGetRecordStmt).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getInt("wr_id");
        verify(mockResultSet).getDate("last_edit_date");

        System.out.println("--- Finished testGetWorkoutRecordByUserId ---");
    }

    @Test
    void testGetWorkoutRecordByUserId_NotFound() throws SQLException {
        System.out.println("--- Starting testGetWorkoutRecordByUserId_NotFound ---");
        int userId = 99; // An ID that should not have a workout record

        // Specific mock for this test
        PreparedStatement mockGetRecordStmt = mock(PreparedStatement.class);
        String sql = "SELECT wr.wr_id, wr.last_edit_date " +
                "FROM WorkoutRecords wr " +
                "JOIN WorkoutRecords_AppUser wra ON wr.wr_id = wra.wr_id " +
                "WHERE wra.user_id = ?";

        // Configure Mocks
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockGetRecordStmt);
        when(mockGetRecordStmt.executeQuery()).thenReturn(mockResultSet); // Using the class-level mockResultSet
        when(mockResultSet.next()).thenReturn(false); // Setting behavior on the class-level mockResultSet

        // Execute
        WorkoutRecord workoutRecord = traineeDAO.getWorkoutRecordByUserId(userId);

        // Assertions
        assertNull(workoutRecord);

        // Verifications
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockGetRecordStmt).setInt(1, userId);
        verify(mockGetRecordStmt).executeQuery();
        verify(mockResultSet).next();

        System.out.println("--- Finished testGetWorkoutRecordByUserId_NotFound ---");
    }
}