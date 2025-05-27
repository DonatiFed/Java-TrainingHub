package ORM;

import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.ExerciseIntensitySetter;
import Model.WorkoutManagement.ExerciseStrategyFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDAO {
    private final Connection connection;

    public ExerciseDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    // New constructor for testing
    public ExerciseDAO(Connection connection) {
        this.connection = connection;
    }

 // adding exercises that are part of the workout records
public Exercise addExercise(String name, String description, String equipment, int sets, int reps, int weight, String strategyType) throws SQLException{
        // Validate inputs before attempting to insert
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Exercise name cannot be empty.");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Exercise description cannot be empty.");
        }
        if (sets <= 0) {
            throw new IllegalArgumentException("Number of sets must be greater than 0.");
        }
        if (reps <= 0) {
            throw new IllegalArgumentException("Number of reps must be greater than 0.");
        }
        if (weight < 0)  {
            throw new IllegalArgumentException("Weight must be greater or equal than 0");
        }
        if (strategyType == null || strategyType.isEmpty()) {
            throw new IllegalArgumentException("Strategy type cannot be empty.");
        }

        // Adjust the sequence before inserting
        adjustSequence();

        String sql = "INSERT INTO Exercises (exercise_name, exercise_description, exercise_equipment, exercise_N_sets, exercise_N_reps, exercise_weight, exercise_strategy) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, equipment);
            stmt.setInt(4, sets);
            stmt.setInt(5, reps);
            stmt.setInt(6, weight);
            stmt.setString(7, strategyType);

            int affectedRows = stmt.executeUpdate();
            System.out.println("affectedRows in addExercise: " + affectedRows); // Debugging line


            if (affectedRows == 0) {
                throw new SQLException("Exercise insertion failed, no rows affected.");
            }

            // Retrieve the generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    ExerciseIntensitySetter strategy = ExerciseStrategyFactory.createStrategy(strategyType);

                    // Return Exercise object with correct ID
                    return new Exercise(generatedId, name, description, equipment, sets, reps, weight, strategy);
                } else {
                    throw new SQLException("Failed to retrieve ID after inserting Exercise.");
                }
            }
        }
    }

//adding exercise that will be part of the workoutplan
    public Exercise addExercise4plan(String name, String description, String equipment, String strategyType) throws SQLException {
        // Validate inputs
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Exercise name cannot be empty.");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Exercise description cannot be empty.");
        }
        if (strategyType == null || strategyType.isEmpty()) {
            throw new IllegalArgumentException("Strategy type cannot be empty.");
        }

        ExerciseIntensitySetter strategy = ExerciseStrategyFactory.createStrategy(strategyType);

        // Step 1: Check for existing exercise with same name, strategy, and NULL weight
        String checkSql = "SELECT * FROM Exercises WHERE exercise_name = ? AND exercise_strategy = ? AND exercise_weight IS NULL";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, name);
            checkStmt.setString(2, strategyType);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Already exists — just return it
                    int existingId = rs.getInt(1);
                    String existingDescription = rs.getString("exercise_description");
                    String existingEquipment = rs.getString("exercise_equipment");

                    System.out.println("Exercise already exists for plan: " + name + ", strategy: " + strategyType);
                    return new Exercise(existingId, name, existingDescription, existingEquipment, strategy);
                }
            }
        }

        // Step 2: Adjust the sequence before inserting new one
          adjustSequence();

        // Step 3: Proceed with insertion
        String insertSql = "INSERT INTO Exercises (exercise_name, exercise_description, exercise_equipment, exercise_N_sets, exercise_N_reps, exercise_weight, exercise_strategy) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, equipment);
            stmt.setInt(4, strategy.setNSets());
            stmt.setInt(5, strategy.setNReps());
            stmt.setNull(6, java.sql.Types.INTEGER); // NULL weight = it's for plan
            stmt.setString(7, strategyType);

            int affectedRows = stmt.executeUpdate();
            System.out.println("affectedRows in addExercise: " + affectedRows);

            if (affectedRows == 0) {
                throw new SQLException("Exercise insertion failed, no rows affected.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    return new Exercise(generatedId, name, description, equipment, strategy);
                } else {
                    throw new SQLException("Failed to retrieve ID after inserting Exercise.");
                }
            }
        }
    }



    // addexercise 4 plan, strategy setter prima della insert, controllo che esiste un esercizio con nome e strategy uguale, il pt basta che prenda quello per metterlo nel plan )
    //( booleano per capire se l'es è del plan o per record )
    // però no booleano per workout4record di 2 trainee che svolgono stesso es stesse rep pk dovrebbe esssere colonna in piu non boooleana  , ma con id trainee

    // Only adjust sequence if there's actually a gap
    private void adjustSequence() {
        try {
            // Get the maximum ID from the table
            String maxIdSql = "SELECT COALESCE(MAX(ex_id), 0) FROM exercises";
            int maxId = 0;

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(maxIdSql)) {
                if (rs.next()) {
                    maxId = rs.getInt(1);
                }
            }

            // Set the sequence to maxId + 1 using setval()
            // setval() returns a value, so we need to use executeQuery()
            if (maxId > 0) {
                String setvalSql = "SELECT setval('exercises_ex_id_seq', ?)";
                try (PreparedStatement resetStmt = connection.prepareStatement(setvalSql)) {
                    resetStmt.setInt(1, maxId + 1); // Next value will be maxId + 1
                    try (ResultSet seqRs = resetStmt.executeQuery()) {
                        // Consume the result (we don't need the value, just need to execute)
                        if (seqRs.next()) {
                            System.out.println("Sequence adjusted to: " + seqRs.getInt(1));
                        }
                    }
                }
            } else {
                // If table is empty, reset sequence to 1
                String setvalSql = "SELECT setval('exercises_ex_id_seq', 1, false)"; // false means next nextval() will return 1
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(setvalSql)) {
                    if (rs.next()) {
                        System.out.println("Sequence reset to start from: 1");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adjusting sequence: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM public.exercises";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("ex_id");
                String name = rs.getString("exercise_name");
                String description = rs.getString("exercise_description");
                String equipment = rs.getString("exercise_equipment");
                int sets = rs.getInt("exercise_N_sets");
                int reps = rs.getInt("exercise_N_reps");
                int weight = rs.getInt("exercise_weight");
                String strategyType = rs.getString("exercise_strategy");

                // Use Factory Pattern to create the strategy object
                ExerciseIntensitySetter strategy = ExerciseStrategyFactory.createStrategy(strategyType);

                // Create Exercise Object
                Exercise exercise = new Exercise(id,name, description, equipment, sets, reps, weight, strategy);
                exercises.add(exercise);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }

    public Exercise getExerciseById(int id) {
        String sql = "SELECT * FROM public.exercises WHERE ex_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("exercise_name");
                    String description = rs.getString("exercise_description");
                    String equipment = rs.getString("exercise_equipment");
                    int sets = rs.getInt("exercise_N_sets");
                    int reps = rs.getInt("exercise_N_reps");
                    int weight = rs.getInt("exercise_weight");
                    String strategyType = rs.getString("exercise_strategy");

                    // Create strategy using factory pattern
                    ExerciseIntensitySetter strategy = ExerciseStrategyFactory.createStrategy(strategyType);

                    return new Exercise(id,name, description, equipment, sets, reps, weight, strategy);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    //  UPDATE: Modify an exercise
    public void updateExercise(int id, String name, String description, String equipment, int sets, int reps, int weight, String strategyType) {
        String sql = "UPDATE Exercises SET exercise_name = ?, exercise_description = ?, exercise_equipment = ?, exercise_N_sets = ?, exercise_N_reps = ?, exercise_weight = ?, exercise_strategy = ? WHERE ex_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, equipment);
            stmt.setInt(4, sets);
            stmt.setInt(5, reps);
            stmt.setInt(6, weight);
            stmt.setString(7, strategyType);
            stmt.setInt(8, id);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Exercise updated successfully!");
            } else {
                System.out.println(" No exercise found with ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //  DELETE: Remove an exercise
    public void deleteExercise(int id) {
        String sql = "DELETE FROM Exercises WHERE ex_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println(" Exercise deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

