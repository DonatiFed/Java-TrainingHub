package BusinessLogic;

import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.Workout4Plan;
import ORM.ExerciseDAO;
import ORM.Workout4PlanDAO;

import java.util.List;

public class Workout4PlanController {
    private final Workout4PlanDAO w4pDAO;

    // Default constructor
    public Workout4PlanController() {
        this.w4pDAO = new Workout4PlanDAO();
    }

    // Constructor with DAO injection (for testing/mocking)
    public Workout4PlanController(Workout4PlanDAO w4pDAO) {
        this.w4pDAO = w4pDAO;
    }

    public Workout4Plan createWorkout4Plan(String dayOfWeek, String strategyType) {
        if (dayOfWeek == null || dayOfWeek.isBlank() || strategyType == null || strategyType.isBlank()) {
            System.err.println("Day of week and strategy type cannot be null or blank.");
            return null;
        }
        Workout4Plan plan = w4pDAO.addWorkout4Plan(dayOfWeek, strategyType);
        if (plan != null) {
            System.out.println("Workout4Plan created successfully.");
        } else {
            System.out.println("Failed to create Workout4Plan.");
        }
        return plan;
    }

    public List<Workout4Plan> getAllWorkout4Plans() {
        List<Workout4Plan> plans = w4pDAO.getAllWorkout4Plans();
        if (plans == null || plans.isEmpty()) {
            System.out.println("No Workout4Plans found.");
        }
        return plans;
    }

    public Workout4Plan getWorkout4PlanById(int id) {
        if (id <= 0) {
            System.err.println("Invalid Workout4Plan ID.");
            return null;
        }
        Workout4Plan plan = w4pDAO.getWorkout4PlanById(id);
        if (plan == null) {
            System.out.println("Workout4Plan not found for ID: " + id);
        }
        return plan;
    }

    public void updateWorkout4Plan(int id, String newDayOfWeek, String newStrategy) {
        if (id <= 0 || newDayOfWeek == null || newDayOfWeek.isBlank() || newStrategy == null || newStrategy.isBlank()) {
            System.err.println("Invalid inputs for updating Workout4Plan.");
            return;
        }
        w4pDAO.updateWorkout4Plan(id, newDayOfWeek, newStrategy);
        System.out.println("Workout4Plan updated successfully.");
    }

    public void deleteWorkout4Plan(int id) {
        if (id <= 0) {
            System.err.println("Invalid Workout4Plan ID.");
            return;
        }
        w4pDAO.deleteWorkout4Plan(id);
        System.out.println("Workout4Plan deleted successfully.");
    }

    public void addExerciseToWorkout4Plan(int w4pId, int exId) {
        if (w4pId <= 0 || exId <= 0) {
            System.err.println("Invalid IDs for Workout4Plan or Exercise.");
            return;
        }

        Workout4Plan plan = w4pDAO.getWorkout4PlanById(w4pId);
        ExerciseDAO exerciseDAO = new ExerciseDAO();
        Exercise exercise = exerciseDAO.getExerciseById(exId);


        if (plan == null) {
            System.err.println("Workout4Plan not found with ID: " + w4pId);
            return;
        }

        if (exercise == null) {
            System.err.println("Exercise not found with ID: " + exId);
            return;
        }

        if (!plan.getStrategy().toString().equalsIgnoreCase(exercise.getStrategy().toString())) {
            System.err.println("Cannot add Exercise with strategy '" + exercise.getStrategy().toString() +
                    "' to Workout4Plan with strategy '" + plan.getStrategy().toString() + "'.");
            return;
        }

        w4pDAO.addExerciseToWorkout4Plan(w4pId, exId);
        System.out.println("Exercise added to Workout4Plan successfully.");
    }

    public List<Exercise> getExercisesForWorkout4Plan(int w4pId) {
        if (w4pId <= 0) {
            System.err.println("Invalid Workout4Plan ID.");
            return null;
        }
        List<Exercise> exercises = w4pDAO.getExercisesForWorkout4Plan(w4pId);
        if (exercises == null || exercises.isEmpty()) {
            System.out.println("No exercises found for Workout4Plan ID: " + w4pId);
        }
        return exercises;
    }

}
