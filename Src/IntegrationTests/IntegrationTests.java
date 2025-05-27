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
            // Delete dependent tables first (respecting FK order)
            statement.executeUpdate("DELETE FROM Workout4Record_WorkoutRecords");
            statement.executeUpdate("DELETE FROM WorkoutPlans_Workout4Plans");
            statement.executeUpdate("DELETE FROM WorkoutRecords_AppUser");
            statement.executeUpdate("DELETE FROM WorkoutPlans_PersonalTrainer_AppUser");

            // Delete base tables (include missing ones!)
            statement.executeUpdate("DELETE FROM Workout4Record");
            statement.executeUpdate("DELETE FROM WorkoutRecords");
            statement.executeUpdate("DELETE FROM WorkoutPlans");
            statement.executeUpdate("DELETE FROM Workout4Plan"); // ✅ this was missing!
            statement.executeUpdate("DELETE FROM Personal_Trainer");
            statement.executeUpdate("DELETE FROM AppUser");
            statement.executeUpdate("DELETE FROM Exercises"); // If you have it

            // Reset sequences to match empty tables
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

        ptController.followTrainee(testPT.getId(), testTrainee.getId(), testWorkoutPlan.getId());

        List<WorkoutPlan> traineePlans = traineeController.getWorkoutPlansByTraineeId(testTrainee.getId());
        assertNotNull(traineePlans);
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

    @Test
    void testExercisesInsideWorkout4PlansAreOfSameStrategy() throws SQLException {
        // Setup
        PersonalTrainerDAO ptDAO = new PersonalTrainerDAO(connection);
        TraineeDAO traineeDAO = new TraineeDAO(connection);
        WorkoutRecordDAO wrDAO = new WorkoutRecordDAO(connection);
        WorkoutPlanDAO wpDAO = new WorkoutPlanDAO(connection);
        Workout4PlanDAO w4pDAO = new Workout4PlanDAO(connection);
        ExerciseDAO exDAO = new ExerciseDAO(connection);

        PersonalTrainerController ptController = new PersonalTrainerController(ptDAO, wpDAO, wrDAO);
        TraineeController traineeController = new TraineeController(traineeDAO, wpDAO, wrDAO);
        WorkoutPlanController wpController = new WorkoutPlanController(wpDAO);
        Workout4PlanController w4pController = new Workout4PlanController(w4pDAO);
        ExerciseController exController = new ExerciseController(exDAO);

        // Create entities
        PersonalTrainer pt = ptController.registerPersonalTrainer("Trainer John", 35);
        Trainee trainee = traineeController.registerTrainee("Trainee Mike", 28);
        WorkoutPlan plan = wpController.createWorkoutPlan();
        ptController.followTrainee(pt.getId(), trainee.getId(), plan.getId());

        // ✅ Test: Matching strategy - should succeed
        Exercise exercise1 = exController.createExerciseForPlan("Chest press", "Panca Piana", "Bilanciere", "Endurance");
        Workout4Plan w4p_1 = w4pController.createWorkout4Plan("Monday", "Endurance");

        // Verify both have the same strategy
        assertEquals("Endurance", exercise1.getStrategy().toString(), "Exercise should have Endurance strategy");
        assertEquals("Endurance", w4p_1.getStrategy().toString(), "Workout4Plan should have Endurance strategy");

        // This should succeed without any issues
        w4pController.addExerciseToWorkout4Plan(w4p_1.getId(), exercise1.getId());
        wpController.addWorkout4PlanToWorkoutPlan(plan.getId(), w4p_1.getId());

        System.out.println("Passed: Matching strategies work correctly");

        //  Test: Mismatching strategy - should be prevented
        Exercise mismatchedExercise = exController.createExerciseForPlan("Squat", "Barbell squat", "Barbell", "Strength");

        // Verify they have different strategies
        assertEquals("Strength", mismatchedExercise.getStrategy().toString(), "Mismatched exercise should have Strength strategy");
        assertEquals("Endurance", w4p_1.getStrategy().toString(), "Workout4Plan should still have Endurance strategy");
        assertNotEquals(mismatchedExercise.getStrategy().toString(), w4p_1.getStrategy().toString(),
                "Strategies should be different");

        // Use assertThrows to check for an exception (if your logic throws one)
        // If your logic doesn't throw an exception but prints to System.err,
        // we need to capture that output.

        java.io.PrintStream originalErr = System.err;
        java.io.ByteArrayOutputStream errContent = new java.io.ByteArrayOutputStream();
        System.setErr(new java.io.PrintStream(errContent));

        w4pController.addExerciseToWorkout4Plan(w4p_1.getId(), mismatchedExercise.getId());

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Cannot add Exercise with strategy 'Strength'"),
                "Should contain error about Strength strategy");
        assertTrue(errorOutput.contains("to Workout4Plan with strategy 'Endurance'"),
                "Should contain error about Endurance strategy");

        System.out.println(" Passed: Strategy mismatch correctly prevented");
        System.out.println("   Captured error message: " + errorOutput.trim());

        // Restore original System.err
        System.setErr(originalErr);

        // Optional: Verify that the mismatched exercise was NOT actually added to the database
         List<Exercise> exercisesInPlan = w4pController.getExercisesForWorkout4Plan(w4p_1.getId());
         assertFalse(exercisesInPlan.contains(mismatchedExercise), "Mismatched exercise should not be in the plan");
    }



}