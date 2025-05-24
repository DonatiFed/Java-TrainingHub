package BusinessLogic;

import Model.WorkoutManagement.WorkoutRecord;
import Model.WorkoutManagement.Workout4Record;
import ORM.WorkoutRecordDAO;

import java.util.List;

public class WorkoutRecordController {
    private final WorkoutRecordDAO workoutRecordDAO;

    // Default constructor
    public WorkoutRecordController() {
        this.workoutRecordDAO = new WorkoutRecordDAO();
    }

    // Constructor for dependency injection
    public WorkoutRecordController(WorkoutRecordDAO workoutRecordDAO) {
        this.workoutRecordDAO = workoutRecordDAO;
    }

    // Create a new WorkoutRecord
    public WorkoutRecord createWorkoutRecord() {
        return workoutRecordDAO.addWorkoutRecord();
    }

    // Get a specific WorkoutRecord by ID
    public WorkoutRecord getWorkoutRecordById(int wrId) {
        if (wrId <= 0) {
            System.err.println("Invalid WorkoutRecord ID.");
            return null;
        }
        return workoutRecordDAO.getWorkoutRecordById(wrId);
    }

    // Get all WorkoutRecords
    public List<WorkoutRecord> getAllWorkoutRecords() {
        return workoutRecordDAO.getAllWorkoutRecords();
    }

    // Delete a WorkoutRecord by ID
    public void deleteWorkoutRecord(int wrId) {
        if (wrId <= 0) {
            System.err.println("Invalid WorkoutRecord ID.");
            return;
        }
        workoutRecordDAO.deleteWorkoutRecord(wrId);
    }

    // Link an existing Workout4Record to a WorkoutRecord
    public void linkWorkout4RecordToWorkoutRecord(int wrId, int w4rId) {
        if (wrId <= 0 || w4rId <= 0) {
            System.err.println("Invalid WorkoutRecord ID or Workout4Record ID.");
            return;
        }
        workoutRecordDAO.addWorkout4RecordToWorkoutRecord(wrId, w4rId);
    }

    // Get all Workout4Records for a WorkoutRecord
    public List<Workout4Record> getWorkout4RecordsForWorkoutRecord(int wrId) {
        if (wrId <= 0) {
            System.err.println("Invalid WorkoutRecord ID.");
            return null;
        }
        return workoutRecordDAO.getWorkout4RecordsByWorkoutRecordId(wrId);
    }
}
