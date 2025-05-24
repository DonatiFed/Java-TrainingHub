package BusinessLogic;

import Model.WorkoutManagement.Workout4Plan;
import Model.WorkoutManagement.Exercise;
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

    // CREATE: Add a Workout4Plan
    public Workout4Plan createWorkout4Plan(String dayOfWeek, String strategyType) {
        if (dayOfWeek == null || dayOfWeek.isBlank() || strategyType == null || strategyType.isBlank()) {
            System.err.println("Day of week and strategy type cannot be null or blank.");
            return null;
        }
        return w4pDAO.addWorkout4Plan(dayOfWeek, strategyType);
    }

    // READ: Get all Workout4Plans
    public List<Workout4Plan> getAllWorkout4Plans() {
        return w4pDAO.getAllWorkout4Plans();
    }

    // READ: Get Workout4Plan by ID
    public Workout4Plan getWorkout4PlanById(int id) {
        if (id <= 0) {
            System.err.println("Invalid Workout4Plan ID.");
            return null;
        }
        return w4pDAO.getWorkout4PlanById(id);
    }

    // UPDATE: Modify Workout4Plan (day or strategy)
    public void updateWorkout4Plan(int id, String newDayOfWeek, String newStrategy) {
        if (id <= 0 || newDayOfWeek == null || newDayOfWeek.isBlank() || newStrategy == null || newStrategy.isBlank()) {
            System.err.println("Invalid inputs for updating Workout4Plan.");
            return;
        }
        w4pDAO.updateWorkout4Plan(id, newDayOfWeek, newStrategy);
    }

    // DELETE: Remove a Workout4Plan
    public void deleteWorkout4Plan(int id) {
        if (id <= 0) {
            System.err.println("Invalid Workout4Plan ID.");
            return;
        }
        w4pDAO.deleteWorkout4Plan(id);
    }

    // ADD: Link an Exercise to a Workout4Plan
    public void addExerciseToWorkout4Plan(int w4pId, int exId) {
        if (w4pId <= 0 || exId <= 0) {
            System.err.println("Invalid IDs for Workout4Plan or Exercise.");
            return;
        }
        w4pDAO.addExerciseToWorkout4Plan(w4pId, exId);
    }

    // GET: Exercises for a Workout4Plan
    public List<Exercise> getExercisesForWorkout4Plan(int w4pId) {
        if (w4pId <= 0) {
            System.err.println("Invalid Workout4Plan ID.");
            return null;
        }
        return w4pDAO.getExercisesForWorkout4Plan(w4pId);
    }
}
