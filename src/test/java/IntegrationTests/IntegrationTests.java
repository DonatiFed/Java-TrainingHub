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
            statement.executeUpdate("DELETE FROM WorkoutPlans");// ✅ this was missing!
            statement.executeUpdate("DELETE FROM Workout4Plan");
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




    /**
     * Tests the scenario that a trainee can view the workout plan assigned to him by their personal trainer.
     */
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



    /**
     * Tests that a personal trainer can view the workout records of a trainee they are assigned to.
     */
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



    /**
     * Tests the constraint that exercises added to a Workout4Plan must have the same strategy
     * as the Workout4Plan itself.
     */
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


    /**
     * Tests the scenario where a personal trainer updates a workout plan that has been assigned to a trainee,
     * ensuring the trainee sees the updated plan.
     */
    @Test
    void testPTUpdatesWorkoutPlanAssignedToTrainee() throws SQLException {
        PersonalTrainerDAO ptDAO = new PersonalTrainerDAO(connection);
        TraineeDAO traineeDAO = new TraineeDAO(connection);
        WorkoutPlanDAO wpDAO = new WorkoutPlanDAO(connection);
        Workout4PlanDAO w4pDAO = new Workout4PlanDAO(connection);
        WorkoutRecordDAO wrDAO = new WorkoutRecordDAO(connection);
        WorkoutPlanController wpController = new WorkoutPlanController(wpDAO);
        PersonalTrainerController ptController = new PersonalTrainerController(ptDAO, wpDAO, wrDAO); // No wrDAO needed here
        TraineeController traineeController = new TraineeController(traineeDAO, wpDAO, wrDAO);     // No wrDAO needed here
        Workout4PlanController w4pController = new Workout4PlanController(w4pDAO);

        // Create PT and Trainee
        PersonalTrainer pt = ptController.registerPersonalTrainer("PT Update", 30);

        PersonalTrainer second_pt = ptController.registerPersonalTrainer("PT Dummy", 35);
        Trainee trainee = traineeController.registerTrainee("Trainee View", 25);

        // Create and assign a WorkoutPlan
        WorkoutPlan initialPlan = wpController.createWorkoutPlan();
        ptController.followTrainee(pt.getId(), trainee.getId(), initialPlan.getId());

        //secondpt plan
        WorkoutPlan secondPlan = wpController.createWorkoutPlan();
        ptController.followTrainee(second_pt.getId(), trainee.getId(), secondPlan.getId());


        // Add a Workout4Plan to the initial WorkoutPlan
        Workout4Plan mondayPlan = w4pController.createWorkout4Plan("Monday", "Endurance");
        wpController.addWorkout4PlanToWorkoutPlan(initialPlan.getId(), mondayPlan.getId());

        Workout4Plan tuesdayPlan = w4pController.createWorkout4Plan("Tuesday", "Hypertrophy");
        wpController.addWorkout4PlanToWorkoutPlan(initialPlan.getId(), tuesdayPlan.getId());

        Workout4Plan fridayPlan = w4pController.createWorkout4Plan("Friday", "Strength");
        wpController.addWorkout4PlanToWorkoutPlan(initialPlan.getId(), fridayPlan.getId());

        // Retrieve the trainee's workout plan and check the initial state
        List<WorkoutPlan> traineePlansInitial = traineeController.getWorkoutPlansByTraineeId(trainee.getId());
        assertTrue(traineePlansInitial.stream().anyMatch(p -> p.getId() == initialPlan.getId()));

        WorkoutPlan traineeViewOfPlansInitial = traineePlansInitial.stream().filter(p -> p.getId() == initialPlan.getId()).findFirst().orElse(null);
        assertNotNull(traineeViewOfPlansInitial);
        assertTrue(wpDAO.getWorkout4PlansByWorkoutPlanId(traineeViewOfPlansInitial.getId()).stream()
                .anyMatch(w4p -> w4p.getId() == mondayPlan.getId() && w4p.getDay().equals("Monday")));

        // PT adds another Workout4Plan to the same WorkoutPlan
        Workout4Plan wednesdayPlan = w4pController.createWorkout4Plan("Wednesday", "Strength");
        wpController.addWorkout4PlanToWorkoutPlan(initialPlan.getId(), wednesdayPlan.getId());

        // Retrieve the trainee's workout plans again and check for the update
        List<WorkoutPlan> traineePlansUpdated = traineeController.getWorkoutPlansByTraineeId(trainee.getId());
        assertTrue(traineePlansUpdated.stream().anyMatch(p -> p.getId() == initialPlan.getId()));

        WorkoutPlan traineeViewOfInitialPlanUpdated = traineePlansUpdated.stream().filter(p -> p.getId() == initialPlan.getId()).findFirst().orElse(null);
        assertNotNull(traineeViewOfInitialPlanUpdated);

        List<Workout4Plan> updatedW4Ps = wpDAO.getWorkout4PlansByWorkoutPlanId(traineeViewOfInitialPlanUpdated.getId());
        assertEquals(4, updatedW4Ps.size());
        assertTrue(updatedW4Ps.stream().anyMatch(w4p -> w4p.getId() == mondayPlan.getId() && w4p.getDay().equals("Monday")));
        assertTrue(updatedW4Ps.stream().anyMatch(w4p -> w4p.getId() == wednesdayPlan.getId() && w4p.getDay().equals("Wednesday")));
    }




    /**
     *
     * Tests the deletion and editing of a Workout4Plan that is part of a WorkoutPlan.
     */
    @Test
    void testDeleteAndEditWorkout4PlanFromWorkoutPlan() throws SQLException {
        WorkoutPlanDAO wpDAO = new WorkoutPlanDAO(connection);
        Workout4PlanDAO w4pDAO = new Workout4PlanDAO(connection);
        WorkoutPlanController wpController = new WorkoutPlanController(wpDAO);
        Workout4PlanController w4pController = new Workout4PlanController(w4pDAO);

        // Create a WorkoutPlan
        WorkoutPlan plan = wpController.createWorkoutPlan();
        assertNotNull(plan);

        // Create a Workout4Plan to add and then potentially edit/delete
        Workout4Plan initialMondayPlan = w4pController.createWorkout4Plan("Monday", "Endurance");
        assertNotNull(initialMondayPlan);
        wpController.addWorkout4PlanToWorkoutPlan(plan.getId(), initialMondayPlan.getId());

        // Create another Workout4Plan
        Workout4Plan fridayPlan = w4pController.createWorkout4Plan("Friday", "Strength");
        assertNotNull(fridayPlan);
        wpController.addWorkout4PlanToWorkoutPlan(plan.getId(), fridayPlan.getId());

        List<Workout4Plan> initialPlansInWorkoutPlan = wpDAO.getWorkout4PlansByWorkoutPlanId(plan.getId());
        assertEquals(2, initialPlansInWorkoutPlan.size());

        // --- Test Deletion ---
        // Delete the Monday plan
        wpDAO.removeWorkout4PlanFromWorkoutPlan(plan.getId(), initialMondayPlan.getId());

        // Verify that it's deleted
        List<Workout4Plan> plansAfterDeletion = wpDAO.getWorkout4PlansByWorkoutPlanId(plan.getId());
        assertEquals(1, plansAfterDeletion.size());
        assertEquals(fridayPlan.getId(), plansAfterDeletion.get(0).getId());

        // --- Test Editing ---
        // Get the remaining Friday plan
        Workout4Plan planToEdit = wpDAO.getWorkout4PlansByWorkoutPlanId(plan.getId()).get(0);
        w4pController.updateWorkout4Plan(planToEdit.getId(), "Saturday", "Hypertrophy");

        // Retrieve the updated plan and verify the changes
        Workout4Plan updatedPlan = w4pController.getWorkout4PlanById(planToEdit.getId());
        assertNotNull(updatedPlan);
        assertEquals("Saturday", updatedPlan.getDay());
        assertEquals("Hypertrophy", updatedPlan.getStrategy().toString());
    }


    /**
     * Tests the process of adding workout sessions (Workout4Records) with multiple exercises
     * to a user's workout history (WorkoutRecord).
     */

    @Test
    void testAddWorkoutSessionsWithMultipleExercisesToUserWorkoutRecord() throws SQLException {
        TraineeDAO traineeDAO = new TraineeDAO(connection);
        WorkoutRecordDAO wrDAO = new WorkoutRecordDAO(connection);
        Workout4RecordDAO w4rDAO = new Workout4RecordDAO(connection); // Corrected to Workout4RecordDAO
        ExerciseDAO exDAO = new ExerciseDAO(connection);
        TraineeController traineeController = new TraineeController(traineeDAO, null, wrDAO);
        WorkoutRecordController wrController = new WorkoutRecordController(wrDAO);
        Workout4RecordController w4rController = new Workout4RecordController(w4rDAO);
        ExerciseController exController = new ExerciseController(exDAO);

        // --- 1. Create a Trainee (and their WorkoutRecord) ---
        Trainee trainee = traineeController.registerTrainee("Multi-Exercise User", 29);
        assertNotNull(trainee);
        WorkoutRecord userWorkoutRecord = wrController.getWorkoutRecordById(trainee.getId());
        assertNotNull(userWorkoutRecord);

        // --- 2. Create Workout4Records (sessions) ---
        Workout4Record session1 = w4rController.createWorkout4Record("2025-05-27");
        assertNotNull(session1);
        Workout4Record session2 = w4rController.createWorkout4Record("2025-05-29");
        assertNotNull(session2);

        // --- 3. Link sessions to the user's record ---
        wrController.linkWorkout4RecordToWorkoutRecord(userWorkoutRecord.getId(), session1.getId());
        wrController.linkWorkout4RecordToWorkoutRecord(userWorkoutRecord.getId(), session2.getId());

        // --- 4. Create multiple Exercises ---
        Exercise benchPress = exController.createExerciseForPlan("Bench Press", "...", "Barbell", "Strength");
        assertNotNull(benchPress);
        Exercise squat = exController.createExerciseForPlan("Squat", "...", "Barbell", "Strength");
        assertNotNull(squat);
        Exercise running = exController.createExerciseForPlan("Running", "...", "Treadmill", "Endurance");
        assertNotNull(running);
        Exercise plank = exController.createExerciseForPlan("Plank", "...", "Bodyweight", "Endurance");
        assertNotNull(plank);

        // --- 5. Add multiple Exercises to the Workout4Records (sessions) ---
        w4rController.addExerciseToWorkout4Record(session1.getId(), benchPress.getId());
        w4rController.addExerciseToWorkout4Record(session1.getId(), squat.getId());
        w4rController.addExerciseToWorkout4Record(session2.getId(), running.getId());
        w4rController.addExerciseToWorkout4Record(session2.getId(), plank.getId());

        // --- 6. Verify the links and contents ---
        // Check linked sessions
        List<Workout4Record> sessionsInHistory = wrController.getWorkout4RecordsForWorkoutRecord(userWorkoutRecord.getId());
        assertNotNull(sessionsInHistory);
        assertEquals(2, sessionsInHistory.size());
        assertTrue(sessionsInHistory.stream().anyMatch(s -> s.getId() == session1.getId()));
        assertTrue(sessionsInHistory.stream().anyMatch(s -> s.getId() == session2.getId()));

        // Check exercises in session 1
        List<Exercise> session1Exercises = w4rController.getExercisesForWorkout4Record(session1.getId());
        assertNotNull(session1Exercises);
        assertEquals(2, session1Exercises.size());
        assertTrue(session1Exercises.stream().anyMatch(e -> e.getId() == benchPress.getId()));
        assertTrue(session1Exercises.stream().anyMatch(e -> e.getId() == squat.getId()));

        // Check exercises in session 2
        List<Exercise> session2Exercises = w4rController.getExercisesForWorkout4Record(session2.getId());
        assertNotNull(session2Exercises);
        assertEquals(2, session2Exercises.size());
        assertTrue(session2Exercises.stream().anyMatch(e -> e.getId() == running.getId()));
        assertTrue(session2Exercises.stream().anyMatch(e -> e.getId() == plank.getId()));
    }
}