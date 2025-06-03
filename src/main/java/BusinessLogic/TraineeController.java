package BusinessLogic;

import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.WorkoutRecord;
import ORM.PersonalTrainerDAO;
import ORM.TraineeDAO;
import ORM.WorkoutPlanDAO;
import ORM.WorkoutRecordDAO;


import java.util.List;


public class TraineeController {
    private final TraineeDAO traineeDAO;
    private final WorkoutPlanDAO wpDAO;
    private final WorkoutRecordDAO wrDAO;

    // Constructor injection of DAOs — must be created elsewhere and passed in
    public TraineeController(TraineeDAO traineeDAO, WorkoutPlanDAO wpDAO, WorkoutRecordDAO wrDAO) {
        this.traineeDAO = traineeDAO;
        this.wpDAO = wpDAO;
        this.wrDAO = wrDAO;
    }

    // Default constructor (optional) - initialize with default DAOs if needed
    public TraineeController() {
        this.traineeDAO = new TraineeDAO();
        this.wpDAO = new WorkoutPlanDAO();
        this.wrDAO = new WorkoutRecordDAO();
    }

    // Create a new Trainee
    public Trainee registerTrainee(String name, int age) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Name cannot be empty.");
            return null;
        }
        if (age <= 0) {
            System.err.println("Age must be positive.");
            return null;
        }

        // Add trainee to the database
        Trainee trainee = traineeDAO.addTrainee(name, age);
        if (trainee == null) {
            System.err.println("Failed to add trainee.");
            return null;
        }

        // Create a workout record for the trainee
        WorkoutRecord workoutRecord = wrDAO.addWorkoutRecord();
        if (workoutRecord == null) {
            System.err.println("Failed to create workout record.");
            return null;
        }
        wrDAO.linkWorkoutRecordToUser(trainee.getId(), workoutRecord.getId());

        return trainee;
    }

    // Get all Trainees
    public List<Trainee> getAllTrainees() {
        return traineeDAO.getAllTrainees();
    }

    // Get a Trainee's Workout Plans
    public List<WorkoutPlan> getWorkoutPlansByTraineeId(int userId) {
        if (userId <= 0) {
            System.err.println("Invalid user ID.");
            return null;
        }
        return traineeDAO.getWorkoutPlanFromUserId(userId);
    }

    // Get Personal Trainer assigned to a Trainee
    public PersonalTrainer getPersonalTrainerForTrainee(int userId) {
        if (userId <= 0) {
            System.err.println("Invalid user ID.");
            return null;
        }
        return traineeDAO.getPTForUserId(userId);
    }

    // Update a Trainee's details
    public void updateTrainee(int userId, String newName, int newAge) {
        if (userId <= 0 || newName == null || newName.trim().isEmpty() || newAge <= 0) {
            System.err.println("Invalid input for updating trainee.");
            return;
        }
        traineeDAO.editUser(userId, newName, newAge);
        System.out.println("Updated trainee with ID: " + userId);
    }

    // Delete a Trainee
    public void deleteTrainee(int userId) {
        if (userId <= 0) {
            System.err.println("Invalid user ID.");
            return;
        }
        traineeDAO.deleteUser(userId);
        System.out.println("Deleted trainee with ID: " + userId);
    }

    // Get WorkoutRecord for Trainee — instantiate DAO here
    public WorkoutRecord getWorkoutRecordByTraineeId(int traineeId) {
        if (traineeId <= 0) {
            System.err.println("Invalid trainee ID.");
            return null;
        }
        WorkoutRecordDAO workoutRecordDAO = new WorkoutRecordDAO();
        return workoutRecordDAO.getWorkoutRecordByUserId(traineeId);
    }
}
