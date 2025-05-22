package ORM.Tests;

import ORM.ExerciseDAO;
import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.ExerciseIntensitySetter;
import Model.WorkoutManagement.ExerciseStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExerciseDAOTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private Statement mockStatement;
    private ResultSet mockResultSet;
    private ExerciseDAO exerciseDAO;
    private ExerciseIntensitySetter mockStrategy;

    @BeforeEach
    void setUp() throws SQLException {
        // Mock the database connection and related objects
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockStatement = mock(Statement.class);
        mockResultSet = mock(ResultSet.class);
        mockStrategy = mock(ExerciseIntensitySetter.class);

        // Configure connection to return our mock prepared statement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Configure statement to return our mock result set
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);

        // Create the DAO with our mock connection
        exerciseDAO = new ExerciseDAO(mockConnection);
    }

    @Test
    void testAddExercise() throws SQLException {
        System.out.println("--- Starting testAddExercise ---");
        // Setup
        String name = "Bench Press";
        String description = "Chest exercise";
        String equipment = "Barbell";
        int sets = 3;
        int reps = 10;
        int weight = 100;
        String strategyType = "Endurance";
        int generatedId = 1;

        // Mock the sequence adjustment result set
        when(mockResultSet.next()).thenReturn(true, true); // First for adjustSequence, second for getGeneratedKeys
        when(mockResultSet.getInt(1)).thenReturn(0, generatedId); // For the MAX query and then for the generated key
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Simulate successful insert

        // Mock the strategy factory to return our mock strategy
        try (MockedStatic<ExerciseStrategyFactory> mockedFactory = Mockito.mockStatic(ExerciseStrategyFactory.class)) {
            mockedFactory.when(() -> ExerciseStrategyFactory.createStrategy(strategyType)).thenReturn(mockStrategy);

            // Execute the method being tested
            Exercise result = exerciseDAO.addExercise(name, description, equipment, sets, reps, weight, strategyType);

            // Verify the result
            assertNotNull(result);
            assertEquals(generatedId, result.getId());
            assertEquals(name, result.getName());
            assertEquals(description, result.getDescription());
            assertEquals(equipment, result.getEquipment());
            assertEquals(sets, result.getN_sets());
            assertEquals(reps, result.getN_reps());
            assertEquals(weight, result.getWeight());
            assertSame(mockStrategy, result.getStrategy());

            // Verify interactions
            verify(mockPreparedStatement).setString(1, name);
            verify(mockPreparedStatement).setString(2, description);
            verify(mockPreparedStatement).setString(3, equipment);
            verify(mockPreparedStatement).setInt(4, sets);
            verify(mockPreparedStatement).setInt(5, reps);
            verify(mockPreparedStatement).setInt(6, weight);
            verify(mockPreparedStatement).setString(7, strategyType);
            verify(mockPreparedStatement).executeUpdate();
            verify(mockPreparedStatement).getGeneratedKeys();
        }
        System.out.println("--- Finished testAddExercise ---");
    }

    @Test
    void testAddExerciseWithInvalidInputs() {
        System.out.println("--- Starting testAddExerciseWithInvalidInputs ---");
        // Test with invalid name
        assertThrows(IllegalArgumentException.class, () ->
                exerciseDAO.addExercise("", "description", "equipment", 3, 10, 100, "Progressive"));

        // Test with invalid description
        assertThrows(IllegalArgumentException.class, () ->
                exerciseDAO.addExercise("Exercise", "", "equipment", 3, 10, 100, "Progressive"));

        // Test with invalid sets
        assertThrows(IllegalArgumentException.class, () ->
                exerciseDAO.addExercise("Exercise", "description", "equipment", 0, 10, 100, "Progressive"));

        // Test with invalid reps
        assertThrows(IllegalArgumentException.class, () ->
                exerciseDAO.addExercise("Exercise", "description", "equipment", 3, 0, 100, "Progressive"));

        // Test with invalid strategy
        assertThrows(IllegalArgumentException.class, () ->
                exerciseDAO.addExercise("Exercise", "description", "equipment", 3, 10, 100, ""));
        System.out.println("--- Finished testAddExerciseWithInvalidInputs ---");
    }


    @Test
    void testAddExercise4plan_Nocollision() throws SQLException {

        System.out.println("--- Starting testAddExercise4plan_nocollision ---");

        //setup
        String name = "Bench Press";
        String description = "Chest exercise";
        String equipment = "Barbell";
        String strategyType = "Endurance";
        int generatedId = 1;
        int expectedSets = 3;
        int expectedReps = 15;

        // Mocking ResultSet sequence
        when(mockResultSet.next()).thenReturn(false, true, true);
        when(mockResultSet.getInt(1)).thenReturn(0, generatedId);

        // Simulate successful insert
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Simulate creation of strategy
        try (MockedStatic<ExerciseStrategyFactory> mockedFactory = Mockito.mockStatic(ExerciseStrategyFactory.class)) {
            mockedFactory.when(() -> ExerciseStrategyFactory.createStrategy(strategyType)).thenReturn(mockStrategy);

            // Strategy behavior stubbing
            when(mockStrategy.setNSets()).thenReturn(expectedSets);
            when(mockStrategy.setNReps()).thenReturn(expectedReps);

            Exercise result = exerciseDAO.addExercise4plan(name, description, equipment, strategyType);

            assertNotNull(result);
            assertEquals(generatedId, result.getId());
            assertEquals(expectedSets, result.getN_sets());
            assertEquals(expectedReps, result.getN_reps());
            assertEquals(0, result.getWeight(), "Weight should default to 0 for plan exercises");
            assertSame(mockStrategy, result.getStrategy());

            // --- Verify interactions on the SHARED mockPreparedStatement ---
            // *** Correction: Expect setString(1, name) twice ***
            // Because the DAO code calls it once for the check query
            // and once for the insert query on the SAME mock object.
            verify(mockPreparedStatement, times(2)).setString(eq(1), eq(name));

            // Verify parameters set ONLY during INSERT (called once each)
            verify(mockPreparedStatement).setString(eq(2), eq(description));
            verify(mockPreparedStatement).setString(eq(3), eq(equipment));
            verify(mockPreparedStatement).setInt(eq(4), eq(expectedSets));
            verify(mockPreparedStatement).setInt(eq(5), eq(expectedReps));
            verify(mockPreparedStatement).setNull(eq(6), eq(java.sql.Types.INTEGER));
            verify(mockPreparedStatement).setString(eq(7), eq(strategyType));

            // Verify execution methods called once each in this "no collision" path
            verify(mockPreparedStatement).executeQuery(); // From the check statement
            verify(mockPreparedStatement).executeUpdate(); // From the insert statement
            verify(mockPreparedStatement).getGeneratedKeys(); // From the insert statement

            // Verify adjustSequence was called via mockStatement (called once)
            verify(mockStatement).executeQuery(startsWith("SELECT MAX"));

        }

        System.out.println("--- Finished testAddExercise4plan_nocollision ---");
    } // End of test method

    @Test
    void testAddExercise4plan_collision() throws SQLException {
        System.out.println("--- Starting testAddExercise4plan_collision ---");

        // --- Given: Data for the collision scenario ---
        String name = "Existing Press"; // Name that exists
        String inputDescription = "This description should NOT be used"; // Input desc for the call
        String inputEquipment = "This equipment should NOT be used"; // Input equip for the call
        String strategyType = "Strength"; // Strategy that exists for the name
        int existingId = 5; // The ID of the existing record in the DB
        String existingDescription = "The OLD Description from DB"; // The description in the DB
        String existingEquipment = "The OLD Equipment from DB"; // The equipment in the DB
        int expectedSetsForStrategy = 5; // Example sets for Strength (used by constructor)
        int expectedRepsForStrategy = 5; // Example reps for Strength (used by constructor)

        // --- Mocking for the COLLISION path ---

        // 1. Mock the check query's PreparedStatement execution
        //    (It uses the shared mockPreparedStatement)
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // 2. Mock the ResultSet from the check query: Indicate a record was found
        when(mockResultSet.next()).thenReturn(true); // Collision found!

        // 3. Mock the data retrieval from the existing record in the ResultSet
        //    NOTE: Using "exercise_id" as per the DAO code provided, although "ex_id" might be correct schema.
        when(mockResultSet.getInt(eq("exercise_id"))).thenReturn(existingId);
        when(mockResultSet.getString(eq("exercise_description"))).thenReturn(existingDescription);
        when(mockResultSet.getString(eq("exercise_equipment"))).thenReturn(existingEquipment);

        // --- Mock the static factory (needed even in collision path per DAO code) ---
        try (MockedStatic<ExerciseStrategyFactory> mockedFactory = Mockito.mockStatic(ExerciseStrategyFactory.class)) {
            mockedFactory.when(() -> ExerciseStrategyFactory.createStrategy(strategyType)).thenReturn(mockStrategy);

            // Stub strategy methods (needed for the constructor called on return)
            when(mockStrategy.setNSets()).thenReturn(expectedSetsForStrategy);
            when(mockStrategy.setNReps()).thenReturn(expectedRepsForStrategy);

            // --- When: Execute the method ---
            Exercise result = exerciseDAO.addExercise4plan(name, inputDescription, inputEquipment, strategyType);

            // --- Then: Assertions ---
            // Verify the returned object matches the EXISTING exercise data (retrieved via ResultSet)
            // but constructed using the "4Plan" constructor from the DAO code.
            assertNotNull(result);
            assertEquals(existingId, result.getId(), "Should return the ID of the existing exercise");
            assertEquals(name, result.getName(), "Name should match the input name used for lookup"); // DAO uses input 'name' in constructor
            assertEquals(existingDescription, result.getDescription(), "Description should be from the existing DB record");
            assertEquals(existingEquipment, result.getEquipment(), "Equipment should be from the existing DB record");
            assertEquals(expectedSetsForStrategy, result.getN_sets(), "Sets should be determined by strategy called by constructor");
            assertEquals(expectedRepsForStrategy, result.getN_reps(), "Reps should be determined by strategy called by constructor");
            assertEquals(0, result.getWeight(), "Weight should be default 0 from '4Plan' constructor used on return");
            assertSame(mockStrategy, result.getStrategy(), "Strategy object should be the one created");

            // --- Then: Verifications ---
            // Verify only the CHECK query path was executed

            // Verify check statement preparation (using anyString here for simplicity, could be more specific)
            verify(mockConnection).prepareStatement(anyString()); // Called once for the check query

            // Verify check statement parameter setting and execution
            verify(mockPreparedStatement).setString(eq(1), eq(name)); // Called once for check
            verify(mockPreparedStatement).setString(eq(2), eq(strategyType)); // Called once for check
            verify(mockPreparedStatement).executeQuery(); // Called once for check

            // Verify INSERT statement and related operations were NEVER called
            verify(mockPreparedStatement, never()).executeUpdate();
            verify(mockPreparedStatement, never()).getGeneratedKeys();
            // Verify insert-specific parameters were never set
            verify(mockPreparedStatement, never()).setString(eq(2), eq(inputDescription)); // Param 2 set only with strategyType
            verify(mockPreparedStatement, never()).setString(eq(3), eq(inputEquipment));
            verify(mockPreparedStatement, never()).setInt(eq(4), anyInt());
            verify(mockPreparedStatement, never()).setInt(eq(5), anyInt());
            verify(mockPreparedStatement, never()).setNull(eq(6), anyInt());
            verify(mockPreparedStatement, never()).setString(eq(7), eq(strategyType)); // Param 7 never set

            // Verify adjustSequence was NEVER called (uses mockStatement)
            verify(mockStatement, never()).executeQuery(anyString());

            // Verify factory was called (DAO code calls it before returning existing)
            mockedFactory.verify(() -> ExerciseStrategyFactory.createStrategy(strategyType));
            // Verify strategy methods were called by constructor
            verify(mockStrategy).setNSets();
            verify(mockStrategy).setNReps();

        } // End try-with-resources

        System.out.println("--- Finished testAddExercise4plan_collision ---");
    }


    @Test
    void testAddExercise4planWithInvalidInputs() { // Keeping original name as requested
        System.out.println("--- Starting testAddExercise4planWithInvalidInputs ---");

        // Define valid inputs to use when testing invalid ones for addExercise4plan
        String Name = "Valid Plan Exercise";
        String Description = "Valid Plan Description";
        String Equipment = "Any Equipment";
        String Strategy = "Strength"; // Or any other valid strategy type

        assertThrows(IllegalArgumentException.class, () ->
                        exerciseDAO.addExercise4plan("", Description, Equipment, Strategy),
                "Empty name should throw IllegalArgumentException for addExercise4plan");

        assertThrows(IllegalArgumentException.class, () ->
                        exerciseDAO.addExercise4plan(null, Description, Equipment, Strategy),
                "Null name should throw IllegalArgumentException for addExercise4plan");

        assertThrows(IllegalArgumentException.class, () ->
                        exerciseDAO.addExercise4plan(Name, "", Equipment, Strategy),
                "Empty description should throw IllegalArgumentException for addExercise4plan");

        assertThrows(IllegalArgumentException.class, () ->
                        exerciseDAO.addExercise4plan(Name, null, Equipment, Strategy),
                "Null description should throw IllegalArgumentException for addExercise4plan");

        assertThrows(IllegalArgumentException.class, () ->
                        exerciseDAO.addExercise4plan(Name, Description, Equipment, ""),
                "Empty strategy type should throw IllegalArgumentException for addExercise4plan");

        assertThrows(IllegalArgumentException.class, () ->
                        exerciseDAO.addExercise4plan(Name, Description, Equipment, null),
                "Null strategy type should throw IllegalArgumentException for addExercise4plan");

        // The checks for sets <= 0 and reps <= 0 were removed as they don't apply to addExercise4plan

        System.out.println("--- Finished testAddExercise4planWithInvalidInputs ---");
    }
    @Test
    void testMockExecuteUpdate() throws SQLException {
        System.out.println("--- Starting testMockExecuteUpdate ---");
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        assertEquals(0, mockPreparedStatement.executeUpdate());
        System.out.println("--- Finished testMockExecuteUpdate ---");
    }

    @Test
    void testAddExerciseFailedInsertion() throws SQLException {
        System.out.println("--- Starting testAddExerciseFailedInsertion ---");
        // Setup
        String name = "Bench Press";
        String description = "Chest exercise";
        String equipment = "Barbell";
        int sets = 3;
        int reps = 10;
        int weight = 100;
        String strategyType = "Progressive";

        // Mock the sequence adjustment result set
        when(mockResultSet.next()).thenReturn(false); // Simulate no existing max ID

        // Mock the failed insertion
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // Simulate failed insert

        // Mock the strategy factory (if needed)
        try (MockedStatic<ExerciseStrategyFactory> mockedFactory = Mockito.mockStatic(ExerciseStrategyFactory.class)) {
            mockedFactory.when(() -> ExerciseStrategyFactory.createStrategy(strategyType)).thenReturn(mockStrategy);

            SQLException caughtException = null;

            try {
                exerciseDAO.addExercise(name, description, equipment, sets, reps, weight, strategyType);
            } catch (SQLException e) {
                caughtException = e;
            }

            assertNotNull(caughtException, "SQLException was not caught.");
            assertEquals("Exercise insertion failed, no rows affected.", caughtException.getMessage());
        }
        System.out.println("--- Finished testAddExerciseFailedInsertion ---");
    }

    @Test
    void testGetAllExercises() throws SQLException {
        System.out.println("--- Starting testGetAllExercises ---");
        // Setup
        when(mockResultSet.next()).thenReturn(true, true, false); // Two exercises
        when(mockResultSet.getInt("ex_id")).thenReturn(1, 2);
        when(mockResultSet.getString("exercise_name")).thenReturn("Exercise 1", "Exercise 2");
        when(mockResultSet.getString("exercise_description")).thenReturn("Description 1", "Description 2");
        when(mockResultSet.getString("exercise_equipment")).thenReturn("Equipment 1", "Equipment 2");
        when(mockResultSet.getInt("exercise_N_sets")).thenReturn(3, 4);
        when(mockResultSet.getInt("exercise_N_reps")).thenReturn(10, 12);
        when(mockResultSet.getInt("exercise_weight")).thenReturn(100, 150);
        when(mockResultSet.getString("exercise_strategy")).thenReturn("Progressive", "Standard");

        // Mock the strategy factory
        try (MockedStatic<ExerciseStrategyFactory> mockedFactory = Mockito.mockStatic(ExerciseStrategyFactory.class)) {
            mockedFactory.when(() -> ExerciseStrategyFactory.createStrategy(anyString())).thenReturn(mockStrategy);

            // Execute the method
            List<Exercise> exercises = exerciseDAO.getAllExercises();

            // Verify
            assertEquals(2, exercises.size());
            assertEquals(1, exercises.get(0).getId());
            assertEquals("Exercise 1", exercises.get(0).getName());
            assertEquals(2, exercises.get(1).getId());
            assertEquals("Exercise 2", exercises.get(1).getName());

            // Verify query execution
            verify(mockConnection).createStatement();
            verify(mockStatement).executeQuery("SELECT * FROM public.exercises");
        }
        System.out.println("--- Finished testGetAllExercises ---");
    }

    @Test
    void testGetExerciseById() throws SQLException {
        System.out.println("--- Starting testGetExerciseById ---");
        // Setup
        int id = 1;
        when(mockResultSet.next()).thenReturn(true); // Exercise found
        when(mockResultSet.getString("exercise_name")).thenReturn("Bench Press");
        when(mockResultSet.getString("exercise_description")).thenReturn("Chest exercise");
        when(mockResultSet.getString("exercise_equipment")).thenReturn("Barbell");
        when(mockResultSet.getInt("exercise_N_sets")).thenReturn(3);
        when(mockResultSet.getInt("exercise_N_reps")).thenReturn(10);
        when(mockResultSet.getInt("exercise_weight")).thenReturn(100);
        when(mockResultSet.getString("exercise_strategy")).thenReturn("Progressive");

        // Mock the strategy factory
        try (MockedStatic<ExerciseStrategyFactory> mockedFactory = Mockito.mockStatic(ExerciseStrategyFactory.class)) {
            mockedFactory.when(() -> ExerciseStrategyFactory.createStrategy(anyString())).thenReturn(mockStrategy);

            // Execute
            Exercise exercise = exerciseDAO.getExerciseById(id);

            // Verify
            assertNotNull(exercise);
            assertEquals(id, exercise.getId());
            assertEquals("Bench Press", exercise.getName());
            assertEquals("Chest exercise", exercise.getDescription());
            assertEquals("Barbell", exercise.getEquipment());
            assertEquals(3, exercise.getN_sets());
            assertEquals(10, exercise.getN_reps());
            assertEquals(100, exercise.getWeight());
            assertSame(mockStrategy, exercise.getStrategy());

            // Verify query parameters
            verify(mockPreparedStatement).setInt(1, id);
            verify(mockPreparedStatement).executeQuery();
        }
        System.out.println("--- Finished testGetExerciseById ---");
    }

    @Test
    void testGetExerciseByIdNotFound() throws SQLException {
        System.out.println("--- Starting testGetExerciseByIdNotFound ---");
        // Setup for exercise not found
        when(mockResultSet.next()).thenReturn(false);

        // Execute
        Exercise exercise = exerciseDAO.getExerciseById(99);

        // Verify
        assertNull(exercise);
        verify(mockPreparedStatement).setInt(1, 99);
        verify(mockPreparedStatement).executeQuery();
        System.out.println("--- Finished testGetExerciseByIdNotFound ---");
    }

    @Test
    void testUpdateExercise() throws SQLException {
        System.out.println("--- Starting testUpdateExercise ---");
        // Setup
        int id = 1;
        String name = "Updated Exercise";
        String description = "Updated description";
        String equipment = "Updated equipment";
        int sets = 4;
        int reps = 12;
        int weight = 120;
        String strategyType = "Standard";

        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Successful update

        // Execute
        exerciseDAO.updateExercise(id, name, description, equipment, sets, reps, weight, strategyType);

        // Verify parameters
        verify(mockPreparedStatement).setString(1, name);
        verify(mockPreparedStatement).setString(2, description);
        verify(mockPreparedStatement).setString(3, equipment);
        verify(mockPreparedStatement).setInt(4, sets);
        verify(mockPreparedStatement).setInt(5, reps);
        verify(mockPreparedStatement).setInt(6, weight);
        verify(mockPreparedStatement).setString(7, strategyType);
        verify(mockPreparedStatement).setInt(8, id);
        verify(mockPreparedStatement).executeUpdate();
        System.out.println("--- Finished testUpdateExercise ---");
    }

    @Test
    void testUpdateExerciseNotFound() throws SQLException {
        System.out.println("--- Starting testUpdateExerciseNotFound ---");
        // Setup for exercise not found
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Execute
        exerciseDAO.updateExercise(99, "Exercise", "Description", "Equipment", 3, 10, 100, "Progressive");

        // Verify
        verify(mockPreparedStatement).executeUpdate();
        System.out.println("--- Finished testUpdateExerciseNotFound ---");
    }

    @Test
    void testDeleteExercise() throws SQLException {
        System.out.println("--- Starting testDeleteExercise ---");
        // Setup
        int id = 1;

        // Execute
        exerciseDAO.deleteExercise(id);

        // Verify
        verify(mockPreparedStatement).setInt(1, id);
        verify(mockPreparedStatement).executeUpdate();
        System.out.println("--- Finished testDeleteExercise ---");
    }
}