package BusinessLogic;

import Model.WorkoutManagement.Exercise;
import ORM.ExerciseDAO;

import java.sql.SQLException;
import java.util.List;

public class ExerciseController {
    private final ExerciseDAO exerciseDAO;

    // Default constructor
    public ExerciseController() {
        this.exerciseDAO = new ExerciseDAO();
    }

    // Constructor for testing or custom DAO
    public ExerciseController(ExerciseDAO exerciseDAO) {
        this.exerciseDAO = exerciseDAO;
    }

    public Exercise createExercise(String name, String description, String equipment,
                                   int sets, int reps, int weight, String strategyType) {
        try {
            return exerciseDAO.addExercise(name, description, equipment, sets, reps, weight, strategyType);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error creating exercise: " + e.getMessage());
            return null;
        }
    }

    public Exercise createExerciseForPlan(String name, String description, String equipment, String strategyType) {
        try {
            return exerciseDAO.addExercise4plan(name, description, equipment, strategyType);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error creating plan-based exercise: " + e.getMessage());
            return null;
        }
    }

    public List<Exercise> getAllExercises() {
        return exerciseDAO.getAllExercises();
    }

    public Exercise getExerciseById(int id) {
        return exerciseDAO.getExerciseById(id);
    }

    public void updateExercise(int id, String name, String description, String equipment,
                               int sets, int reps, int weight, String strategyType) {
        try {
            exerciseDAO.updateExercise(id, name, description, equipment, sets, reps, weight, strategyType);
        } catch (Exception e) {
            System.err.println("Error updating exercise: " + e.getMessage());
        }
    }

    public void deleteExercise(int id) {
        try {
            exerciseDAO.deleteExercise(id);
        } catch (Exception e) {
            System.err.println("Error deleting exercise: " + e.getMessage());
        }
    }
}
