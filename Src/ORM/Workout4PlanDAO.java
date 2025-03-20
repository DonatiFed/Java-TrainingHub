package ORM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Workout4PlanDAO {
    private final Connection connection;

    public Workout4PlanDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    //  CREATE: Add a Workout4Plan
    public void addWorkout4Plan(String dayOfWeek, String strategy) {
        String sql = "INSERT INTO Workout4Plans (day_of_week, strategy) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dayOfWeek);
            stmt.setString(2, strategy);
            stmt.executeUpdate();
            System.out.println(" Workout4Plan added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  READ: Get all Workout4Plans
    public List<String> getAllWorkout4Plans() {
        List<String> workoutPlans = new ArrayList<>();
        String sql = "SELECT * FROM Workout4Plans";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                workoutPlans.add("ID: " + rs.getInt("w4p_id") + ", Day: " + rs.getString("day_of_week") + ", Strategy: " + rs.getString("strategy"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutPlans;
    }

    //  UPDATE: Modify Day of the Week or Strategy
    public void updateWorkout4Plan(int id, String newDayOfWeek, String newStrategy) {
        String sql = "UPDATE Workout4Plans SET day_of_week = ?, strategy = ? WHERE w4p_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newDayOfWeek);
            stmt.setString(2, newStrategy);
            stmt.setInt(3, id);
            stmt.executeUpdate();
            System.out.println(" Workout4Plan updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  DELETE: Remove a Workout4Plan
    public void deleteWorkout4Plan(int id) {
        String sql = "DELETE FROM Workout4Plans WHERE w4p_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println(" Workout4Plan deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ADD EXERCISES: Insert into the relationship table
    public void addExerciseToWorkout4Plan(int w4pId, int exId) {
        String sql = "INSERT INTO Workout4Plan_Exercises (w4p_id, ex_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, w4pId);
            stmt.setInt(2, exId);
            stmt.executeUpdate();
            System.out.println(" Exercise added to Workout4Plan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  GET EXERCISES for a Workout4Plan
    public List<String> getExercisesForWorkout4Plan(int w4pId) {
        List<String> exercises = new ArrayList<>();
        String sql = "SELECT e.exercise_name FROM Exercises e " +
                "JOIN Workout4Plan_Exercises wpe ON e.ex_id = wpe.ex_id " +
                "WHERE wpe.w4p_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, w4pId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    exercises.add(rs.getString("exercise_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }
}


