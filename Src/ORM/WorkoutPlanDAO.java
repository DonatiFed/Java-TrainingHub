package ORM;

import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.Workout4Plan;
import Model.UserManagement.Observer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanDAO {
    private final Connection connection;

    public WorkoutPlanDAO() {
        this.connection = DatabaseManager.getConnection();
    }
    //Todo: Last_edit_date is dateType in the db, but a string for the WorkoutPlanClass

    // CREATE: Insert a new WorkoutPlan
    public WorkoutPlan addWorkoutPlan() {
        String sql = "INSERT INTO WorkoutPlans (last_edit_date) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            java.sql.Date currentDate = java.sql.Date.valueOf(java.time.LocalDate.now());
            stmt.setDate(1, currentDate);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating WorkoutPlan failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new WorkoutPlan(id);  // Creating WorkoutPlan object
                } else {
                    throw new SQLException("Creating WorkoutPlan failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ: Get all WorkoutPlans
    public List<WorkoutPlan> getAllWorkoutPlans() {
        List<WorkoutPlan> workoutPlans = new ArrayList<>();
        String sql = "SELECT * FROM WorkoutPlans";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("wp_id");
                java.sql.Date lastEditDate = rs.getDate("last_edit_date");

                WorkoutPlan plan = new WorkoutPlan( id); // No observer at this stage
                String lastEditDateStr = (lastEditDate != null) ? lastEditDate.toString() : null;
                plan.setLastEditDate(lastEditDateStr);

                // Fetch associated Workout4Plans
                plan.getWorkouts().addAll(getWorkout4PlansByWorkoutPlanId(id));

                workoutPlans.add(plan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutPlans;
    }

    // READ: Get WorkoutPlan by ID
    public WorkoutPlan getWorkoutPlanById(int wpId) {
        String sql = "SELECT * FROM WorkoutPlans WHERE wp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date lastEditDate = rs.getDate("last_edit_date");
                    WorkoutPlan plan = new WorkoutPlan(wpId);

                    String lastEditDateStr = (lastEditDate != null) ? lastEditDate.toString() : null;
                    plan.setLastEditDate(lastEditDateStr);
                    // Fetch associated Workout4Plans
                    plan.getWorkouts().addAll(getWorkout4PlansByWorkoutPlanId(wpId));

                    return plan;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // DELETE: Remove a WorkoutPlan
    public void deleteWorkoutPlan(int wpId) {
        String sql = "DELETE FROM WorkoutPlans WHERE wp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            stmt.executeUpdate();
            System.out.println("WorkoutPlan deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GET Workout4Plans for a given WorkoutPlan (returns Workout4Plan objects)
    public List<Workout4Plan> getWorkout4PlansByWorkoutPlanId(int wpId) {
        List<Workout4Plan> workout4Plans = new ArrayList<>();
        String sql = "SELECT w4p.w4p_id, w4p.day, w4p.strategy " +
                "FROM Workout4Plans w4p " +
                "JOIN WorkoutPlans_Workout4Plans wpp ON w4p.w4p_id = wpp.w4p_id " +
                "WHERE wpp.wp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("w4p_id");
                    String dayOfWeek = rs.getString("day");
                    String strategy = rs.getString("strategy");

                    Workout4Plan workout4Plan = new Workout4Plan(dayOfWeek, strategy,id );
                    workout4Plans.add(workout4Plan);
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


