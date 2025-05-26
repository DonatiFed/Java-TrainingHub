package IntegrationTests;

import BusinessLogic.*;
import ORM.*;
import Model.UserManagement.*;
import Model.WorkoutManagement.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTests {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DatabaseManager.getConnection();
        clearDatabase(connection);
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        DatabaseManager.closeConnection();
    }


    private void clearDatabase(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM Workout4Record_WorkoutRecords");
            statement.executeUpdate("DELETE FROM WorkoutPlans_Workout4Plans");
            statement.executeUpdate("DELETE FROM WorkoutRecords_AppUser");
            statement.executeUpdate("DELETE FROM WorkoutPlans_PersonalTrainer_AppUser");
            statement.executeUpdate("DELETE FROM Personal_Trainer");
            statement.executeUpdate("DELETE FROM AppUser");
            statement.executeUpdate("DELETE FROM WorkoutPlans");
            statement.executeUpdate("DELETE FROM WorkoutRecords");
            statement.executeUpdate("DELETE FROM Workout4Record");

            statement.executeUpdate("ALTER SEQUENCE appuser_user_id_seq RESTART WITH 1");
            statement.executeUpdate("ALTER SEQUENCE exercises_ex_id_seq RESTART WITH 1");
            statement.executeUpdate("ALTER SEQUENCE workout4plan_w4p_id_seq RESTART WITH 1");
            statement.executeUpdate("ALTER SEQUENCE workout4record_w4r_id_seq RESTART WITH 1");
            statement.executeUpdate("ALTER SEQUENCE workoutplans_wp_id_seq RESTART WITH 1");
            statement.executeUpdate("ALTER SEQUENCE workoutrecords_wr_id_seq RESTART WITH 1");
        }
    }

    @Test
    void testTraineeCanViewWorkoutPlanAssignedByTheirPT() throws SQLException {
        // DAOs and controllers use connection initialized by @BeforeEach
        PersonalTrainerDAO ptDAO = new PersonalTrainerDAO(connection);
        TraineeDAO traineeDAO = new TraineeDAO(connection);
        WorkoutPlanDAO wpDAO = new WorkoutPlanDAO(connection);
        WorkoutRecordDAO wrDAO = new WorkoutRecordDAO(connection);

        PersonalTrainerController ptController = new PersonalTrainerController(ptDAO, wpDAO, wrDAO);
        TraineeController traineeController = new TraineeController(traineeDAO, wpDAO, wrDAO);
        WorkoutPlanController wpController = new WorkoutPlanController(wpDAO);

        PersonalTrainer testPT = ptController.registerPersonalTrainer("Test PT", 30);
        assertNotNull(testPT);

        Trainee testTrainee = traineeController.registerTrainee("Test Trainee", 25);
        assertNotNull(testTrainee);

        WorkoutPlan testWorkoutPlan = wpController.createWorkoutPlan();
        assertNotNull(testWorkoutPlan);

        ptController.assignWorkoutPlanToTrainee(testPT.getId(), testTrainee.getId(), testWorkoutPlan.getId());

        List<WorkoutPlan> traineePlans = traineeController.getWorkoutPlansByTraineeId(testTrainee.getId());
        assertNotNull(traineePlans);
        assertFalse(traineePlans.isEmpty());
        assertTrue(traineePlans.stream().anyMatch(plan -> plan.getId() == testWorkoutPlan.getId()));
    }

    @Test
    void testPersonalTrainerCanViewTraineeWorkoutRecords() throws SQLException {
        // DAOs and controllers use connection initialized by @BeforeEach
        PersonalTrainerDAO ptDAO = new PersonalTrainerDAO(connection);
        TraineeDAO traineeDAO = new TraineeDAO(connection);
        WorkoutRecordDAO wrDAO = new WorkoutRecordDAO(connection);
        WorkoutPlanDAO wpDAO = new WorkoutPlanDAO(connection);

        PersonalTrainerController ptController = new PersonalTrainerController(ptDAO, wpDAO, wrDAO);
        TraineeController traineeController = new TraineeController(traineeDAO, wpDAO, wrDAO);
        WorkoutPlanController wpController = new WorkoutPlanController(wpDAO);

        PersonalTrainer pt = ptController.registerPersonalTrainer("Trainer John", 35);
        Trainee trainee = traineeController.registerTrainee("Trainee Mike", 28);

        WorkoutPlan plan = wpController.createWorkoutPlan();
        ptController.followTrainee(pt.getId(), trainee.getId(), plan.getId());

        WorkoutRecord expectedRecord = traineeController.getWorkoutRecordByTraineeId(trainee.getId());
        assertNotNull(expectedRecord);

        WorkoutRecord actualRecord = ptController.getWorkoutRecordForTrainee(pt.getId(), trainee.getId());
        assertNotNull(actualRecord);
        assertEquals(expectedRecord.getId(), actualRecord.getId());
    }
}
