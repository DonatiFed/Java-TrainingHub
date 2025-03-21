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

    //  CREATE: Insert new exercise
    public Exercise addExercise(String name, String description, String equipment, int sets, int reps, int weight, String strategyType) {
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

            if (affectedRows == 0) {
                throw new SQLException("Exercise insertion failed, no rows affected.");
            }

            // Retrieve the generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    ExerciseIntensitySetter strategy = ExerciseStrategyFactory.createStrategy(strategyType);


                    //  Return Exercise object with correct ID
                    return new Exercise(generatedId, name, description, equipment, sets, reps, weight, strategy);
                } else {
                    throw new SQLException("Failed to retrieve ID after inserting Exercise.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Return null if insertion fails
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

