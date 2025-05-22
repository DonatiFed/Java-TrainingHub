package ORM.Tests;

import ORM.DatabaseManager;
import ORM.PersonalTrainerDAO;
import ORM.WorkoutRecordDAO;
import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.UserManagement.User;
import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.WorkoutRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PersonalTrainerDAOTest {

    private Connection mockConnection;
    private Statement mockStatement;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private PersonalTrainerDAO personalTrainerDAO;
    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseManager = Mockito.mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        personalTrainerDAO = new PersonalTrainerDAO();

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Default to 1 row affected
    }

    @AfterEach
    void tearDown() {
        if (mockedDatabaseManager != null) {
            mockedDatabaseManager.close();
        }
    }

    @Test
    void testAddPersonalTrainer() throws SQLException {
        String name = "Jane Doe";
        int age = 35;
        int generatedId = 3;
        int workoutRecordId = 2;

        String sqlUser = "INSERT INTO AppUser (user_name, user_age, is_pt) VALUES (?, ?, true) RETURNING user_id";
        String sqlTrainer = "INSERT INTO Personal_Trainer (pt_id) VALUES (?)";
        String sqlLinkWorkoutRecord = "INSERT INTO WorkoutRecords_AppUser (user_id, wr_id) VALUES (?, ?)";

        // Create separate mocks for each PreparedStatement
        PreparedStatement mockUserStmt = mock(PreparedStatement.class);
        PreparedStatement mockTrainerStmt = mock(PreparedStatement.class);
        PreparedStatement mockLinkStmt = mock(PreparedStatement.class);

        // Mock ResultSet for user ID
        ResultSet mockGeneratedKeysRs = mock(ResultSet.class);
        when(mockUserStmt.executeQuery()).thenReturn(mockGeneratedKeysRs);
        when(mockGeneratedKeysRs.next()).thenReturn(true);
        when(mockGeneratedKeysRs.getInt("user_id")).thenReturn(generatedId);

        // Mock Connection prepareStatements for each SQL
        when(mockConnection.prepareStatement(eq(sqlUser))).thenReturn(mockUserStmt);
        when(mockConnection.prepareStatement(eq(sqlTrainer))).thenReturn(mockTrainerStmt);
        when(mockConnection.prepareStatement(eq(sqlLinkWorkoutRecord))).thenReturn(mockLinkStmt);

        PersonalTrainer result = null;

        try (MockedConstruction<WorkoutRecordDAO> mockedConstruction =
                     Mockito.mockConstruction(WorkoutRecordDAO.class,
                             (mock, context) -> when(mock.addWorkoutRecord())
                                     .thenReturn(new WorkoutRecord(workoutRecordId)))) {

            result = personalTrainerDAO.addPersonalTrainer(name, age);

            // Verify user insert
            verify(mockConnection).prepareStatement(eq(sqlUser));
            verify(mockUserStmt).setString(1, name);
            verify(mockUserStmt).setInt(2, age);
            verify(mockUserStmt).executeQuery();

            // Verify trainer insert
            verify(mockConnection).prepareStatement(eq(sqlTrainer));
            verify(mockTrainerStmt).setInt(1, generatedId);
            verify(mockTrainerStmt).executeUpdate();

            // Verify workout record link insert
            verify(mockConnection).prepareStatement(eq(sqlLinkWorkoutRecord));
            verify(mockLinkStmt).setInt(1, generatedId);
            verify(mockLinkStmt).setInt(2, workoutRecordId);
            verify(mockLinkStmt).executeUpdate();
        }

        assertNotNull(result);
        assertEquals(generatedId, result.getId());
        assertEquals(name, result.getName());
        assertEquals(age, result.getAge());
    }

    @Test
    void testGetAllPersonalTrainers() throws SQLException {
        String sql = "SELECT a.user_id, a.user_name, a.user_age FROM AppUser a JOIN Personal_Trainer pt ON a.user_id = pt.pt_id";

        // Mock ResultSet behavior: 2 rows then end
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("user_id")).thenReturn(3, 4);
        when(mockResultSet.getString("user_name")).thenReturn("Jane Doe", "Peter Pan");
        when(mockResultSet.getInt("user_age")).thenReturn(35, 28);

        // Mock Connection and Statement
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(sql)).thenReturn(mockResultSet);

        // Create a spy of your DAO to stub internal methods
        PersonalTrainerDAO spyDAO = Mockito.spy(personalTrainerDAO);

        // Stub the internal methods to return empty lists (safe stubs)
        doReturn(Collections.emptyList()).when(spyDAO).getUsersOfPT(anyInt());
        doReturn(Collections.emptyList()).when(spyDAO).getPlansMadeByPT(anyInt());

        // Run method under test with spy
        List<PersonalTrainer> trainers = spyDAO.getAllPersonalTrainers();

        System.out.println("DEBUG: Retrieved trainers list:");
        for (PersonalTrainer trainer : trainers) {
            System.out.println("  Trainer ID: " + trainer.getId() + ", Name: " + trainer.getName() + ", Age: " + trainer.getAge());
        }

        // Assertions
        assertNotNull(trainers);
        assertEquals(2, trainers.size());

        assertEquals(3, trainers.get(0).getId());
        assertEquals("Jane Doe", trainers.get(0).getName());
        assertEquals(35, trainers.get(0).getAge());

        assertEquals(4, trainers.get(1).getId());
        assertEquals("Peter Pan", trainers.get(1).getName());
        assertEquals(28, trainers.get(1).getAge());

        // Verify interactions
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(sql);
        verify(mockResultSet, times(3)).next();
    }




    @Test
    void testGetPersonalTrainerById() throws SQLException {
        int ptId = 3;
        String sql = "SELECT a.user_id, a.user_name, a.user_age FROM AppUser a JOIN Personal_Trainer pt ON a.user_id = pt.pt_id WHERE a.user_id = ?";

        // Mock ResultSet to return one row
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("user_name")).thenReturn("Jane Doe");
        when(mockResultSet.getInt("user_age")).thenReturn(35);

        // Mock PreparedStatement and Connection behavior
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // To avoid infinite loops in getUsersOfPT and getPlansMadeByPT (if they do DB calls),
        // either mock them or stub with empty lists. For now, stub with empty lists.
        PersonalTrainerDAO spyDao = Mockito.spy(personalTrainerDAO);
        doReturn(Collections.emptyList()).when(spyDao).getUsersOfPT(ptId);
        doReturn(Collections.emptyList()).when(spyDao).getPlansMadeByPT(ptId);

        // Run the method under test on the spy
        PersonalTrainer trainer = spyDao.getPersonalTrainerById(ptId);

        // Assertions
        assertNotNull(trainer);
        assertEquals(ptId, trainer.getId());                // ptId is passed directly
        assertEquals("Jane Doe", trainer.getName());
        assertEquals(35, trainer.getAge());

        // Verify correct DB interactions
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, ptId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getString("user_name");
        verify(mockResultSet).getInt("user_age");

        // Verify internal DAO calls to getUsersOfPT and getPlansMadeByPT
        verify(spyDao).getUsersOfPT(ptId);
        verify(spyDao).getPlansMadeByPT(ptId);
    }


    @Test
    void testGetPersonalTrainerById_NotFound() throws SQLException {
        int ptId = 3;
        String sql = "SELECT a.user_id, a.user_name, a.user_age FROM AppUser a JOIN Personal_Trainer pt ON a.user_id = pt.pt_id WHERE a.user_id = ?";
        when(mockResultSet.next()).thenReturn(false);
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        PersonalTrainer trainer = personalTrainerDAO.getPersonalTrainerById(ptId);

        assertNull(trainer);
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, ptId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();

        // Removed the invalid verify(...) on personalTrainerDAO
    }


    @Test
    void testGetPlansMadeByPT() throws SQLException {
        int ptId = 3;
        String sql = "SELECT wp.wp_id, wp.last_edit_date FROM WorkoutPlans wp JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON wp.wp_id = wpt.wp_id WHERE wpt.pt_id = ?";
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("wp_id")).thenReturn(101);
        when(mockResultSet.getString("last_edit_date")).thenReturn("2023-10-27");
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<WorkoutPlan> plans = personalTrainerDAO.getPlansMadeByPT(ptId);

        assertNotNull(plans);
        assertEquals(1, plans.size());
        assertEquals(101, plans.get(0).getId());
        assertEquals("2023-10-27", plans.get(0).getLastEditDate());
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, ptId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(2)).next();
        verify(mockResultSet).getInt("wp_id");
        verify(mockResultSet).getString("last_edit_date");
    }

    @Test
    void testGetUsersOfPT() throws SQLException {
        int ptId = 3;
        String sql = "SELECT u.user_id, u.user_name, u.user_age, u.is_pt FROM AppUser u JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON u.user_id = wpt.trainee_id WHERE wpt.pt_id = ?";
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("user_id")).thenReturn(1, 4);
        when(mockResultSet.getString("name")).thenReturn("John Doe", "Peter Pan");
        when(mockResultSet.getInt("age")).thenReturn(30, 28);
        when(mockResultSet.getBoolean("is_pt")).thenReturn(false, true);
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<User> users = personalTrainerDAO.getUsersOfPT(ptId);

        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.get(0) instanceof Trainee);
        assertEquals(1, users.get(0).getId());
        assertEquals("John Doe", users.get(0).getName());
        assertEquals(30, users.get(0).getAge());
        assertTrue(users.get(1) instanceof PersonalTrainer);
        assertEquals(4, users.get(1).getId());
        assertEquals("Peter Pan", users.get(1).getName());
        assertEquals(28, users.get(1).getAge());
        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, ptId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next();
        verify(mockResultSet, times(2)).getInt("user_id");
        verify(mockResultSet, times(2)).getString("name");
        verify(mockResultSet, times(2)).getInt("age");
        verify(mockResultSet, times(2)).getBoolean("is_pt");
    }

    @Test
    void testEditPersonalTrainer() throws SQLException {
        int ptId = 3;
        String newName = "Updated Jane";
        int newAge = 36;
        String sql = "UPDATE AppUser SET user_name = ?, user_age = ? WHERE user_id = ?";
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);

        personalTrainerDAO.editPersonalTrainer(ptId, newName, newAge);

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setString(1, newName);
        verify(mockPreparedStatement).setInt(2, newAge);
        verify(mockPreparedStatement).setInt(3, ptId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDeletePersonalTrainer() throws SQLException {
        int ptId = 3;
        String sql = "DELETE FROM AppUser WHERE user_id = ?";
        when(mockConnection.prepareStatement(eq(sql))).thenReturn(mockPreparedStatement);

        personalTrainerDAO.deletePersonalTrainer(ptId);

        verify(mockConnection).prepareStatement(eq(sql));
        verify(mockPreparedStatement).setInt(1, ptId);
        verify(mockPreparedStatement).executeUpdate();
    }
}