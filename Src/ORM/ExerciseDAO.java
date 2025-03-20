package ORM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDAO {
    private final Connection connection;

    public ExerciseDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    //  CREATE: Insert new exercise
    public void addExercise(String name, String description, String equipment, int sets, int reps, int weight, String strategy) {
        String sql = "INSERT INTO Exercises (exercise_name, exercise_description, exercise_equipment, exercise_N_sets, exercise_N_reps, exercise_weight, exercise_strategy) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, equipment);
            stmt.setInt(4, sets);
            stmt.setInt(5, reps);
            stmt.setInt(6, weight);
            stmt.setString(7, strategy);
            stmt.executeUpdate();
            System.out.println(" Exercise added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  READ: Get all exercises
    public List<String> getAllExercises() {
        List<String> exercises = new ArrayList<>();
        String sql = "SELECT * FROM public.exercises";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                exercises.add(rs.getString("exercise_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }

    //  UPDATE: Modify an exercise
    public void updateExercise(int id, String name, String description, String equipment, int sets, int reps, int weight, String strategy) {
        String sql = "UPDATE Exercises SET exercise_name = ?, exercise_description = ?, exercise_equipment = ?, exercise_N_sets = ?, exercise_N_reps = ?, exercise_weight = ?, exercise_strategy = ? WHERE ex_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, equipment);
            stmt.setInt(4, sets);
            stmt.setInt(5, reps);
            stmt.setInt(6, weight);
            stmt.setString(7, strategy);
            stmt.setInt(8, id);
            stmt.executeUpdate();
            System.out.println(" Exercise updated successfully!");
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

