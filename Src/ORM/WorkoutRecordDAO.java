package ORM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkoutRecordDAO {
    private final Connection connection;

    public WorkoutRecordDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    // CREATE: Insert a new WorkoutRecord (with last_edit_date and N_workouts = 0)
    public void addWorkoutRecord(String lastEditDate) {
        String sql = "INSERT INTO WorkoutRecords (last_edit_date, N_workouts) VALUES (?, 0)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, lastEditDate);
            stmt.executeUpdate();
            System.out.println(" WorkoutRecord added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ: Get all WorkoutRecords
    public List<String> getAllWorkoutRecords() {
        List<String> workoutRecords = new ArrayList<>();
        String sql = "SELECT * FROM WorkoutRecords";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                workoutRecords.add("ID: " + rs.getInt("wr_id") +
                        ", Last Edit: " + rs.getString("last_edit_date") +
                        ", N Workouts: " + rs.getInt("N_workouts"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutRecords;
    }

    // DELETE: Remove a WorkoutRecord
    public void deleteWorkoutRecord(int wrId) {
        String sql = "DELETE FROM WorkoutRecords WHERE wr_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wrId);
            stmt.executeUpdate();
            System.out.println(" WorkoutRecord deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GET Workout4Records by WorkoutRecord ID
    public List<String> getWorkout4RecordsByWorkoutRecordId(int wrId) {
        List<String> workout4Records = new ArrayList<>();
        String sql = "SELECT w4r.w4r_id, w4r.date " +
                "FROM Workout4Record w4r " +
                "JOIN Workout4Record_WorkoutRecords wrr ON w4r.w4r_id = wrr.w4r_id " +
                "WHERE wrr.wr_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wrId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workout4Records.add("W4R_ID: " + rs.getInt("w4r_id") +
                            ", Date: " + rs.getString("date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workout4Records;
    }

    // ADD Workout4Record to WorkoutRecord (Insert into relationship table)
    public void addWorkout4RecordToWorkoutRecord(int wrId, int w4rId) {
        String sql = "INSERT INTO Workout4Record_WorkoutRecords (w4r_id, wr_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, w4rId);
            stmt.setInt(2, wrId);
            stmt.executeUpdate();
            System.out.println("Workout4Record linked to WorkoutRecord!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

