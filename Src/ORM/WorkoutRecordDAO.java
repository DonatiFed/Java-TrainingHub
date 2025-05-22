package ORM;

import Model.WorkoutManagement.WorkoutRecord;
import Model.WorkoutManagement.Workout4Record;
import Model.UserManagement.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkoutRecordDAO {
    private final Connection connection;

    public WorkoutRecordDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    // CREATE: Insert a new WorkoutRecord (returns WorkoutRecord object)
    public WorkoutRecord addWorkoutRecord() {
        String sql = "INSERT INTO WorkoutRecords (last_edit_date, N_workouts) VALUES (?, 0)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            java.sql.Date currentDate = java.sql.Date.valueOf(java.time.LocalDate.now()); // Convert LocalDate to SQL Date
            stmt.setDate(1, currentDate);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating WorkoutRecord failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new WorkoutRecord(id);  // Creating WorkoutRecord object
                } else {
                    throw new SQLException("Creating WorkoutRecord failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ: Get all WorkoutRecords
    public List<WorkoutRecord> getAllWorkoutRecords() {
        List<WorkoutRecord> workoutRecords = new ArrayList<>();
        String sql = "SELECT * FROM WorkoutRecords";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("wr_id");
                String lastEditDate = rs.getDate("last_edit_date").toString(); // Convert SQL Date to String
                int nWorkouts = rs.getInt("N_workouts");

                WorkoutRecord record = new WorkoutRecord(id); // No user reference at this stage
                record.setLastEditDate(lastEditDate);
                record.setnWorkouts(nWorkouts);

                // Fetch associated Workout4Records
                record.getWorkouts().addAll(getWorkout4RecordsByWorkoutRecordId(id));

                workoutRecords.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutRecords;
    }

    // READ: Get WorkoutRecord by ID
    public WorkoutRecord getWorkoutRecordById(int wrId) {
        String sql = "SELECT * FROM WorkoutRecords WHERE wr_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wrId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String lastEditDate = rs.getDate("last_edit_date").toString();
                    int nWorkouts = rs.getInt("N_workouts");

                    WorkoutRecord record = new WorkoutRecord(wrId);
                    record.setLastEditDate(lastEditDate);
                    record.setnWorkouts(nWorkouts);

                    // Fetch associated Workout4Records
                    record.getWorkouts().addAll(getWorkout4RecordsByWorkoutRecordId(wrId));

                    return record;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // DELETE: Remove a WorkoutRecord
    public void deleteWorkoutRecord(int wrId) {
        String sql = "DELETE FROM WorkoutRecords WHERE wr_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wrId);
            stmt.executeUpdate();
            System.out.println("WorkoutRecord deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GET Workout4Records for a given WorkoutRecord (returns Workout4Record objects)
    public List<Workout4Record> getWorkout4RecordsByWorkoutRecordId(int wrId) {
        List<Workout4Record> workout4Records = new ArrayList<>();
        String sql = "SELECT w4r.w4r_id, w4r.date " +
                "FROM Workout4Record w4r " +
                "JOIN Workout4Record_WorkoutRecords wrr ON w4r.w4r_id = wrr.w4r_id " +
                "WHERE wrr.wr_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wrId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("w4r_id");
                    String date = rs.getString("date");

                    Workout4Record workout4Record = new Workout4Record(date, id);
                    workout4Records.add(workout4Record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workout4Records;
    }

    // ADD Workout4Record to WorkoutRecord (Insert into relationship table)
    public void addWorkout4RecordToWorkoutRecord(int wrId, int w4rId) {
        // First, check if the (w4r_id, wr_id) pair already exists
        String checkSql = "SELECT COUNT(*) FROM Workout4Record_WorkoutRecords WHERE w4r_id = ? AND wr_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, w4rId);
            checkStmt.setInt(2, wrId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // If the pair already exists, print a message and return
                    System.out.println("Workout4Record (w4r_id=" + w4rId + ") is already linked to WorkoutRecord (wr_id=" + wrId + "). Skipping insertion.");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return; // Exit if an error occurs
        }

        // If the pair does not exist, proceed with insertion
        String insertSql = "INSERT INTO Workout4Record_WorkoutRecords (w4r_id, wr_id) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setInt(1, w4rId);
            insertStmt.setInt(2, wrId);
            insertStmt.executeUpdate();
            System.out.println("Workout4Record (w4r_id=" + w4rId + ") linked to WorkoutRecord (wr_id=" + wrId + ") successfully!");
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL errors gracefully
        }
    }
}

