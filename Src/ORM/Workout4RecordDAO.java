package ORM;

import Model.WorkoutManagement.Workout4Record;
import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.ExerciseStrategyFactory;
import Model.WorkoutManagement.ExerciseIntensitySetter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Workout4RecordDAO {
    private final Connection connection;

    public Workout4RecordDAO(Connection connection) {
        this.connection = connection;
    }

    public Workout4RecordDAO() {
        this(DatabaseManager.getConnection());
    }


    // CREATE: Add a Workout4Record
    public Workout4Record addWorkout4Record(String date) {
        String sql = "INSERT INTO Workout4Record (date) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating Workout4Record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new Workout4Record(date, id);
                } else {
                    throw new SQLException("Creating Workout4Record failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ: Get all Workout4Records
    public List<Workout4Record> getAllWorkout4Records() {
        List<Workout4Record> workoutRecords = new ArrayList<>();
        String sql = "SELECT * FROM Workout4Record";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("w4r_id");
                String date = rs.getString("date");
                Workout4Record record = new Workout4Record(date, id);
                record.setExercises(getExercisesForWorkout4Record(id)); // Attach exercises
                workoutRecords.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutRecords;
    }

    // READ: Get a Workout4Record by ID
    public Workout4Record getWorkout4RecordById(int id) {
        String sql = "SELECT * FROM Workout4Record WHERE w4r_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String date = rs.getDate("date").toString();
                    Workout4Record record = new Workout4Record(date, id);
                    record.setExercises(getExercisesForWorkout4Record(id)); // Attach exercises
                    return record;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE: Modify Date
    public void updateWorkout4RecordDate(int id, String newDate) {
        String sql = "UPDATE Workout4Record SET date = ? WHERE w4r_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(newDate));
            stmt.setInt(2, id);
            stmt.executeUpdate();
            System.out.println("Workout4Record date updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE: Remove a Workout4Record
    public void deleteWorkout4Record(int id) {
        String sql = "DELETE FROM Workout4Record WHERE w4r_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Workout4Record deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ADD EXERCISES: Insert into the relationship table
    public void addExerciseToWorkout4Record(int w4rId, int exId) {
        // First, check if the (w4r_id, ex_id) pair already exists
        String checkSql = "SELECT COUNT(*) FROM Workout4Record_Exercises WHERE w4r_id = ? AND ex_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, w4rId);
            checkStmt.setInt(2, exId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // If the pair already exists, print a message and return
                    System.out.println("Exercise (ex_id=" + exId + ") is already linked to Workout4Record (w4r_id=" + w4rId + "). Skipping insertion.");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return; // Exit if an error occurs
        }

        // If the pair does not exist, proceed with insertion
        String insertSql = "INSERT INTO Workout4Record_Exercises (w4r_id, ex_id) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setInt(1, w4rId);
            insertStmt.setInt(2, exId);
            insertStmt.executeUpdate();
            System.out.println("Exercise (ex_id=" + exId + ") added to Workout4Record (w4r_id=" + w4rId + ") successfully!");
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL errors gracefully
        }
    }


    // GET EXERCISES for a Workout4Record (returns Exercise objects)
    public List<Exercise> getExercisesForWorkout4Record(int w4rId) {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT e.* FROM Exercises e " +
                "JOIN Workout4Record_Exercises wre ON e.ex_id = wre.ex_id " +
                "WHERE wre.w4r_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, w4rId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("ex_id");
                    String name = rs.getString("exercise_name");
                    String description = rs.getString("exercise_description");
                    String equipment = rs.getString("exercise_equipment");
                    int sets = rs.getInt("exercise_N_sets");
                    int reps = rs.getInt("exercise_N_reps");
                    int weight = rs.getInt("exercise_weight");
                    String strategyStr = rs.getString("exercise_strategy");

                    // Create Strategy object using Factory
                    ExerciseIntensitySetter strategy = ExerciseStrategyFactory.createStrategy(strategyStr);

                    // Create Exercise object
                    Exercise exercise = new Exercise(id, name, description, equipment, sets, reps, weight, strategy);
                    exercises.add(exercise);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }
}


