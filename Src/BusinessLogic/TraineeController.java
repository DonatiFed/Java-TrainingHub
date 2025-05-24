package BusinessLogic;

import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.WorkoutManagement.WorkoutPlan;
import ORM.TraineeDAO;

import java.util.List;

public class TraineeController {
    private final TraineeDAO traineeDAO;

    // Default constructor
    public TraineeController() {
        this.traineeDAO = new TraineeDAO();
    }

    // Custom DAO for testing or injection
    public TraineeController(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
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

        return traineeDAO.addTrainee(name, age);
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
    }

    // Delete a Trainee
    public void deleteTrainee(int userId) {
        if (userId <= 0) {
            System.err.println("Invalid user ID.");
            return;
        }

        traineeDAO.deleteUser(userId);
    }
}
