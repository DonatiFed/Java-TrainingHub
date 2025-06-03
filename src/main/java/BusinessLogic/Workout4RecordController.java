package BusinessLogic;

import Model.WorkoutManagement.Workout4Record;
import Model.WorkoutManagement.Exercise;
import ORM.Workout4RecordDAO;

import java.util.List;

public class Workout4RecordController {
    private final Workout4RecordDAO w4rDAO;

    // Default constructor
    public Workout4RecordController() {
        this.w4rDAO = new Workout4RecordDAO();
    }

    // Constructor with DAO injection (for testing)
    public Workout4RecordController(Workout4RecordDAO w4rDAO) {
        this.w4rDAO = w4rDAO;
    }

    // Create a new Workout4Record
    public Workout4Record createWorkout4Record(String date) {
        if (date == null || date.isBlank()) {
            System.err.println("Date cannot be empty.");
            return null;
        }
        return w4rDAO.addWorkout4Record(date);
    }

    // Get all Workout4Records
    public List<Workout4Record> getAllWorkout4Records() {
        return w4rDAO.getAllWorkout4Records();
    }

    // Get a specific Workout4Record by ID
    public Workout4Record getWorkout4RecordById(int id) {
        if (id <= 0) {
            System.err.println("Invalid Workout4Record ID.");
            return null;
        }
        return w4rDAO.getWorkout4RecordById(id);
    }

    // Update Workout4Record date
    public void updateWorkout4RecordDate(int id, String newDate) {
        if (id <= 0 || newDate == null || newDate.isBlank()) {
            System.err.println("Invalid input for updating Workout4Record.");
            return;
        }
        w4rDAO.updateWorkout4RecordDate(id, newDate);
    }

    // Delete Workout4Record
    public void deleteWorkout4Record(int id) {
        if (id <= 0) {
            System.err.println("Invalid Workout4Record ID.");
            return;
        }
        w4rDAO.deleteWorkout4Record(id);
    }

    // Add an Exercise to a Workout4Record
    public void addExerciseToWorkout4Record(int w4rId, int exId) {
        if (w4rId <= 0 || exId <= 0) {
            System.err.println("Invalid IDs for Workout4Record or Exercise.");
            return;
        }
        w4rDAO.addExerciseToWorkout4Record(w4rId, exId);
    }

    // Get all exercises linked to a Workout4Record
    public List<Exercise> getExercisesForWorkout4Record(int w4rId) {
        if (w4rId <= 0) {
            System.err.println("Invalid Workout4Record ID.");
            return null;
        }
        return w4rDAO.getExercisesForWorkout4Record(w4rId);
    }
}
