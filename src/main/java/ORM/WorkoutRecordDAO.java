package ORM;

import Model.WorkoutManagement.WorkoutRecord;
import Model.WorkoutManagement.Workout4Record;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkoutRecordDAO {
    private final Connection connection;

    public WorkoutRecordDAO(Connection connection) {
        this.connection = connection;
    }

    public WorkoutRecordDAO() {
        this(DatabaseManager.getConnection());
    }

    public WorkoutRecord addWorkoutRecord() {
        String sql = "INSERT INTO WorkoutRecords (last_edit_date, N_workouts) VALUES (?, 0)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new WorkoutRecord(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void linkWorkoutRecordToUser(int userId, int wrId){
        String sql = "INSERT INTO WorkoutRecords_AppUser (user_id, wr_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, wrId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
        e.printStackTrace();
       }
    }

    public WorkoutRecord getWorkoutRecordByUserId(int userId) {
        String sql = "SELECT wr.* FROM WorkoutRecords wr " +
                "JOIN workoutrecords_appuser wau ON wr.wr_id = wau.wr_id " +
                "WHERE wau.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int wr_id = rs.getInt("wr_id");
                    WorkoutRecord record = new WorkoutRecord(wr_id);
                    // If needed, add loading exercises or other details here
                    return record;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // no record found for this user
    }


    public WorkoutRecord getWorkoutRecordById(int wrId) {
        String sql = "SELECT * FROM WorkoutRecords WHERE wr_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wrId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    WorkoutRecord record = new WorkoutRecord(wrId);
                    record.setLastEditDate(rs.getDate("last_edit_date").toString());
                    record.setnWorkouts(rs.getInt("N_workouts"));
                    record.getWorkouts().addAll(getWorkout4RecordsByWorkoutRecordId(wrId));
                    return record;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<WorkoutRecord> getAllWorkoutRecords() {
        List<WorkoutRecord> workoutRecords = new ArrayList<>();
        String sql = "SELECT * FROM WorkoutRecords";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("wr_id");
                WorkoutRecord record = new WorkoutRecord(id);
                record.setLastEditDate(rs.getDate("last_edit_date").toString());
                record.setnWorkouts(rs.getInt("N_workouts"));
                record.getWorkouts().addAll(getWorkout4RecordsByWorkoutRecordId(id));
                workoutRecords.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutRecords;
    }

    public void deleteWorkoutRecord(int wrId) {
        String sql = "DELETE FROM WorkoutRecords WHERE wr_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wrId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Workout4Record> getWorkout4RecordsByWorkoutRecordId(int wrId) {
        List<Workout4Record> workout4Records = new ArrayList<>();
        String sql = "SELECT w4r.w4r_id, w4r.date FROM Workout4Record w4r JOIN Workout4Record_WorkoutRecords wrr ON w4r.w4r_id = wrr.w4r_id WHERE wrr.wr_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, wrId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workout4Records.add(new Workout4Record(rs.getString("date"), rs.getInt("w4r_id")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workout4Records;
    }

    public void addWorkout4RecordToWorkoutRecord(int wrId, int w4rId) {
        String checkSql = "SELECT COUNT(*) FROM Workout4Record_WorkoutRecords WHERE w4r_id = ? AND wr_id = ?";
        String insertSql = "INSERT INTO Workout4Record_WorkoutRecords (w4r_id, wr_id) VALUES (?, ?)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, w4rId);
            checkStmt.setInt(2, wrId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Already linked.");
                    return;
                }
            }
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setInt(1, w4rId);
                insertStmt.setInt(2, wrId);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
