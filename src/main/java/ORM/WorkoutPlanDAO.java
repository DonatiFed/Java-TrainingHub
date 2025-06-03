package ORM;

import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.Workout4Plan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanDAO {
    private final Connection connection;

    public WorkoutPlanDAO(Connection connection) {
        this.connection = connection;
    }

    public WorkoutPlanDAO() {
        this(DatabaseManager.getConnection());
    }

    // CREATE: Insert a new WorkoutPlan
    public WorkoutPlan addWorkoutPlan() {
        String sql = "INSERT INTO WorkoutPlans (last_edit_date) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) throw new SQLException("Creating WorkoutPlan failed, no rows affected.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new WorkoutPlan(id);
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
                WorkoutPlan plan = new WorkoutPlan(id);
                plan.setLastEditDate((lastEditDate != null) ? lastEditDate.toString() : null);
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
                    plan.setLastEditDate((lastEditDate != null) ? lastEditDate.toString() : null);
                    plan.getWorkouts().addAll(getWorkout4PlansByWorkoutPlanId(wpId));
                    return plan;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE: Update last_edit_date
    public boolean updateLastEditDate(int wpId, String newDate) {
        String sql = "UPDATE WorkoutPlans SET last_edit_date = ? WHERE wp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(newDate));
            stmt.setInt(2, wpId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE: Remove a WorkoutPlan
    public void deleteWorkoutPlan(int wpId) {
        String sql = "DELETE FROM WorkoutPlans WHERE wp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // LINK: Get Workout4Plans for a given WorkoutPlan
    public List<Workout4Plan> getWorkout4PlansByWorkoutPlanId(int wpId) {
        List<Workout4Plan> workout4Plans = new ArrayList<>();
        String sql = "SELECT w4p.w4p_id, w4p.day, w4p.strategy FROM Workout4Plan w4p " +
                "JOIN WorkoutPlans_Workout4Plans wpp ON w4p.w4p_id = wpp.w4p_id WHERE wpp.wp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("w4p_id");
                    String dayOfWeek = rs.getString("day");
                    String strategy = rs.getString("strategy");
                    workout4Plans.add(new Workout4Plan(dayOfWeek, strategy, id));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workout4Plans;
    }

    // LINK: Add Workout4Plan to WorkoutPlan
    public void addWorkout4PlanToWorkoutPlan(int wpId, int w4pId) {
        if (!doesIdExist("WorkoutPlans", "wp_id", wpId) || !doesIdExist("Workout4Plan", "w4p_id", w4pId)) return;

        String checkSql = "SELECT COUNT(*) FROM WorkoutPlans_Workout4Plans WHERE wp_id = ? AND w4p_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, wpId);
            checkStmt.setInt(2, w4pId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String insertSql = "INSERT INTO WorkoutPlans_Workout4Plans (wp_id, w4p_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setInt(1, wpId);
            stmt.setInt(2, w4pId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // LINK: Remove Workout4Plan from WorkoutPlan
    public void removeWorkout4PlanFromWorkoutPlan(int wpId, int w4pId) {
        String sql = "DELETE FROM WorkoutPlans_Workout4Plans WHERE wp_id = ? AND w4p_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            stmt.setInt(2, w4pId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // LINK: Assign WorkoutPlan to Trainee and PT
    public void assignWorkoutPlanToUser(int workoutPlanId, int traineeId, int personalTrainerId) {
        if (!doesIdExist("WorkoutPlans", "wp_id", workoutPlanId) ||
                !doesIdExist("AppUser", "user_id", traineeId) ||
                !doesIdExist("AppUser", "user_id", personalTrainerId) ||
                !isPersonalTrainer(personalTrainerId)) return;

        String sql = "INSERT INTO WorkoutPlans_PersonalTrainer_AppUser (wp_id, trainee_id, pt_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, workoutPlanId);
            stmt.setInt(2, traineeId);
            stmt.setInt(3, personalTrainerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // UNLINK: Unassign WorkoutPlan from Trainee and PT
    public void unassignWorkoutPlan(int wpId, int traineeId, int ptId) {
        String sql = "DELETE FROM WorkoutPlans_PersonalTrainer_AppUser WHERE wp_id = ? AND trainee_id = ? AND pt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wpId);
            stmt.setInt(2, traineeId);
            stmt.setInt(3, ptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ: Get all WorkoutPlans assigned to a specific trainee
    public List<WorkoutPlan> getWorkoutPlansByTraineeId(int traineeId) {
        List<WorkoutPlan> plans = new ArrayList<>();
        String sql = "SELECT wp.* FROM WorkoutPlans wp " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpu ON wp.wp_id = wpu.wp_id " +
                "WHERE wpu.trainee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, traineeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("wp_id");
                    String date = rs.getDate("last_edit_date").toString();
                    WorkoutPlan plan = new WorkoutPlan(id);
                    plan.setLastEditDate(date);
                    plan.getWorkouts().addAll(getWorkout4PlansByWorkoutPlanId(id));
                    plans.add(plan);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    // Helper method: check if ID exists
    private boolean doesIdExist(String tableName, String columnName, int id) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper method: check if user is PT
    private boolean isPersonalTrainer(int userId) {
        String sql = "SELECT is_pt FROM AppUser WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean("is_pt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
