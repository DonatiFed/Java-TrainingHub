package ORM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Workout4RecordDAO {
    private final Connection connection;

    public Workout4RecordDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    // CREATE: Add a Workout4Record
    public void addWorkout4Record(int workoutRecordId, String date) {
        String sql = "INSERT INTO Workout4Records (workout_record_id, date) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, workoutRecordId);
            stmt.setString(2, date);
            stmt.executeUpdate();
            System.out.println("Workout4Record added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ: Get all Workout4Records
    public List<String> getAllWorkout4Records() {
        List<String> workoutRecords = new ArrayList<>();
        String sql = "SELECT * FROM Workout4Records";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                workoutRecords.add("ID: " + rs.getInt("w4r_id") + ", Date: " + rs.getString("date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutRecords;
    }

    //  UPDATE: Modify Date
    public void updateWorkout4RecordDate(int id, String newDate) {
        String sql = "UPDATE Workout4Records SET date = ? WHERE w4r_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newDate);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            System.out.println(" Workout4Record date updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  DELETE: Remove a Workout4Record
    public void deleteWorkout4Record(int id) {
        String sql = "DELETE FROM Workout4Records WHERE w4r_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Workout4Record deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  ADD EXERCISES: Insert into the relationship table
    public void addExerciseToWorkout4Record(int w4rId, int exId) {
        String sql = "INSERT INTO Workout4Record_Exercises (w4r_id, ex_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, w4rId);
            stmt.setInt(2, exId);
            stmt.executeUpdate();
            System.out.println("Exercise added to Workout4Record!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GET EXERCISES for a Workout4Record
    public List<String> getExercisesForWorkout4Record(int w4rId) {
        List<String> exercises = new ArrayList<>();
        String sql = "SELECT e.exercise_name FROM Exercises e " +
                "JOIN Workout4Record_Exercises wre ON e.ex_id = wre.ex_id " +
                "WHERE wre.w4r_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, w4rId);
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

