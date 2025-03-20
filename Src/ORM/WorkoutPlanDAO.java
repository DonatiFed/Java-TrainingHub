package ORM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanDAO {
    private final Connection connection;

    public WorkoutPlanDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    //  CREATE: Insert a new WorkoutPlan
    public void addWorkoutPlan(String lastEditDate) {
        String sql = "INSERT INTO WorkoutPlans (last_edit_date) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, lastEditDate);
            stmt.executeUpdate();
            System.out.println(" WorkoutPlan added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ: Get all WorkoutPlans
    public List<String> getAllWorkoutPlans() {
        List<String> workoutPlans = new ArrayList<>();
        String sql = "SELECT * FROM WorkoutPlans";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                workoutPlans.add("ID: " + rs.getInt("wp_id") + ", Last Edit: " + rs.getString("last_edit_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutPlans;
    }

    //  DELETE: Remove a WorkoutPlan
    public void deleteWorkoutPlan(int wpId) {
        String sql = "DELETE FROM WorkoutPlans WHERE wp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            stmt.executeUpdate();
            System.out.println(" WorkoutPlan deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GET Workout4Plans by WorkoutPlan ID
    public List<String> getWorkout4PlansByWorkoutPlanId(int wpId) {
        List<String> workout4Plans = new ArrayList<>();
        String sql = "SELECT w4p.w4p_id, w4p.day_of_week, w4p.strategy " +
                "FROM Workout4Plans w4p " +
                "JOIN WorkoutPlans_Workout4Plans wpp ON w4p.w4p_id = wpp.w4p_id " +
                "WHERE wpp.wp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workout4Plans.add("W4P_ID: " + rs.getInt("w4p_id") +
                            ", Day: " + rs.getString("day_of_week") +
                            ", Strategy: " + rs.getString("strategy"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workout4Plans;
    }

    // ADD Workout4Plans to WorkoutPlan (Insert into relationship table)
    public void addWorkout4PlanToWorkoutPlan(int wpId, int w4pId) {
        String sql = "INSERT INTO WorkoutPlans_Workout4Plans (wp_id, w4p_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            stmt.setInt(2, w4pId);
            stmt.executeUpdate();
            System.out.println("Workout4Plan linked to WorkoutPlan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

