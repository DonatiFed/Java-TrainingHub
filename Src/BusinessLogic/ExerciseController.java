package BusinessLogic;

import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.ExerciseStrategyFactory;
import Model.WorkoutManagement.ExerciseIntensitySetter;
import ORM.ExerciseDAO;
import java.util.List;

public class ExerciseController {
    private ExerciseDAO exerciseDAO;

    public ExerciseController() {
        this.exerciseDAO = new ExerciseDAO();
    }

    //  Create an Exercise using Strategy Pattern
    public void createExercise(String name, String description, String equipment, int weight, String strategyType) {
        try {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException(" Exercise name cannot be empty!");
            }
            if (strategyType == null || strategyType.isEmpty()) {
                throw new IllegalArgumentException(" Strategy type cannot be empty!");
            }

            // Create Strategy using Factory
            ExerciseIntensitySetter strategy = ExerciseStrategyFactory.createStrategy(strategyType);

            // Create Exercise Object with Strategy
            Exercise exercise = new Exercise(name, description, equipment, 0, 0, weight, strategy);
            exercise.configureIntensity(); // Apply strategy

            // Insert into database
            exerciseDAO.addExercise(
                    exercise.getName(),
                    exercise.getDescription(),
                    exercise.getEquipment(),
                    exercise.getN_sets(),
                    exercise.getN_reps(),
                    exercise.getWeight(),
                    strategyType
            );
            System.out.println(" Exercise created successfully!");
        } catch (Exception e) {
            System.err.println(" Error creating exercise: " + e.getMessage());
        }
    }

    //  Edit Exercise by ID with Validation
    public void editExercise(int id, String name, String description, String equipment, int sets, int reps, int weight, String strategyType) {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException(" Invalid exercise ID!");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException(" Exercise name cannot be empty!");
            }

            // Check if exercise exists before updating
            List<String> existingExercises = exerciseDAO.getAllExercises();
            if (!existingExercises.contains(name)) {
                throw new IllegalArgumentException(" Exercise does not exist!");
            }

            ExerciseIntensitySetter strategy = ExerciseStrategyFactory.createStrategy(strategyType);
            Exercise exercise = new Exercise(name, description, equipment, sets, reps, weight, strategy);

            exerciseDAO.updateExercise(
                    id,
                    exercise.getName(),
                    exercise.getDescription(),
                    exercise.getEquipment(),
                    exercise.getN_sets(),
                    exercise.getN_reps(),
                    exercise.getWeight(),
                    strategyType
            );
            System.out.println(" Exercise updated successfully!");
        } catch (Exception e) {
            System.err.println(" Error updating exercise: " + e.getMessage());
        }
    }

    //  Delete Exercise by ID with Validation
    public void deleteExercise(int id) {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException(" Invalid exercise ID!");
            }

            exerciseDAO.deleteExercise(id);
            System.out.println("Exercise deleted successfully!");
        } catch (Exception e) {
            System.err.println("Error deleting exercise: " + e.getMessage());
        }
    }

    // ✅ Get All Exercises with Handling
    public List<String> getAllExercises() {
        try {
            List<String> exercises = exerciseDAO.getAllExercises();
            if (exercises.isEmpty()) {
                System.out.println("⚠️ No exercises found.");
            }
            return exercises;
        } catch (Exception e) {
            System.err.println(" Error fetching exercises: " + e.getMessage());
            return null;
        }
    }
}


