package BusinessLogic;

import Model.WorkoutManagement.WorkoutPlan;
import ORM.WorkoutPlanDAO;

import java.util.List;

public class WorkoutPlanController {
    private final WorkoutPlanDAO workoutPlanDAO;

    // Default constructor
    public WorkoutPlanController() {
        this.workoutPlanDAO = new WorkoutPlanDAO();
    }

    // Constructor for dependency injection (e.g., testing)
    public WorkoutPlanController(WorkoutPlanDAO workoutPlanDAO) {
        this.workoutPlanDAO = workoutPlanDAO;
    }

    // Create new WorkoutPlan
    public WorkoutPlan createWorkoutPlan() {
        return workoutPlanDAO.addWorkoutPlan();
    }

    // Get all WorkoutPlans
    public List<WorkoutPlan> getAllWorkoutPlans() {
        return workoutPlanDAO.getAllWorkoutPlans();
    }

    // Get specific WorkoutPlan by ID
    public WorkoutPlan getWorkoutPlanById(int wpId) {
        if (wpId <= 0) {
            System.err.println("Invalid WorkoutPlan ID.");
            return null;
        }
        return workoutPlanDAO.getWorkoutPlanById(wpId);
    }

    // Delete WorkoutPlan by ID
    public void deleteWorkoutPlan(int wpId) {
        if (wpId <= 0) {
            System.err.println("Invalid WorkoutPlan ID.");
            return;
        }
        workoutPlanDAO.deleteWorkoutPlan(wpId);
    }

    // Add Workout4Plan to WorkoutPlan
    public void addWorkout4PlanToWorkoutPlan(int wpId, int w4pId) {
        if (wpId <= 0 || w4pId <= 0) {
            System.err.println("Invalid WorkoutPlan ID or Workout4Plan ID.");
            return;
        }

        workoutPlanDAO.addWorkout4PlanToWorkoutPlan(wpId, w4pId);
        System.out.println("Workout4Plan added to WorkoutPlan successfully.");
    }
}
