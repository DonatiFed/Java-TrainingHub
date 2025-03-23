package ORM;

import Model.WorkoutManagement.Workout4Plan;
import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.ExerciseStrategyFactory;
import Model.WorkoutManagement.ExerciseIntensitySetter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//TODO: gestire workout4plan/workout4plans, in modo di avere un'unica istanza tra db e table correlate
public class Workout4PlanDAO {
    private final Connection connection;

    public Workout4PlanDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    // CREATE: Add a Workout4Plan
    public Workout4Plan addWorkout4Plan(String dayOfWeek, String strategyType) {
        String sql = "INSERT INTO Workout4Plan (day, strategy) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dayOfWeek);
            stmt.setString(2, strategyType);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted == 0) {
                System.out.println(" No Workout4Plan was added.");
                return null;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    System.out.println(" Workout4Plan added successfully with ID: " + generatedId);
                    return new Workout4Plan(dayOfWeek, strategyType, generatedId);  // Return the created object
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // READ: Get all Workout4Plans
    public List<Workout4Plan> getAllWorkout4Plans() {
        List<Workout4Plan> workoutPlans = new ArrayList<>();
        String sql = "SELECT * FROM Workout4Plan";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("w4p_id");
                String dayOfWeek = rs.getString("day");
                String strategy = rs.getString("strategy");
                ExerciseIntensitySetter strategyObj = ExerciseStrategyFactory.createStrategy(strategy);
                Workout4Plan workout = new Workout4Plan(dayOfWeek, strategy, id);
                workout.setExercises(getExercisesForWorkout4Plan(id)); // Attach exercises
                workoutPlans.add(workout);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutPlans;
    }

    // READ: Get a Workout4Plan by ID
    public Workout4Plan getWorkout4PlanById(int id) {
        String sql = "SELECT * FROM Workout4Plan WHERE w4p_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dayOfWeek = rs.getString("day");
                    String strategy = rs.getString("strategy");
                    ExerciseIntensitySetter strategyObj = ExerciseStrategyFactory.createStrategy(strategy);
                    Workout4Plan workout = new Workout4Plan(dayOfWeek, strategy, id);
                    workout.setExercises(getExercisesForWorkout4Plan(id)); // Attach exercises
                    return workout;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE: Modify Day of the Week or Strategy
    public void updateWorkout4Plan(int id, String newDayOfWeek, String newStrategy) {
        String sql = "UPDATE Workout4Plan SET day = ?, strategy = ? WHERE w4p_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newDayOfWeek);
            stmt.setString(2, newStrategy);
            stmt.setInt(3, id);
            stmt.executeUpdate();
            System.out.println("Workout4Plan updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE: Remove a Workout4Plan
    public void deleteWorkout4Plan(int id) {
        String sql = "DELETE FROM Workout4Plan WHERE w4p_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Workout4Plan deleted successfully!");
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
            System.out.println("Exercise added to Workout4Plan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GET EXERCISES for a Workout4Plan (returns Exercise objects)
    public List<Exercise> getExercisesForWorkout4Plan(int w4pId) {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT e.* FROM Exercises e " +
                "JOIN Workout4Plan_Exercises wpe ON e.ex_id = wpe.ex_id " +
                "WHERE wpe.w4p_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, w4pId);
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



