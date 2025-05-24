package BusinessLogic;

import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.UserManagement.User;
import Model.WorkoutManagement.WorkoutPlan;
import ORM.PersonalTrainerDAO;

import java.util.List;

public class PersonalTrainerController {
    private final PersonalTrainerDAO ptDAO;

    // Default constructor
    public PersonalTrainerController() {
        this.ptDAO = new PersonalTrainerDAO();
    }

    // Custom DAO constructor (for testing/injection)
    public PersonalTrainerController(PersonalTrainerDAO ptDAO) {
        this.ptDAO = ptDAO;
    }

    // Create a new Personal Trainer
    public PersonalTrainer registerPersonalTrainer(String name, int age) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Name cannot be empty.");
            return null;
        }
        if (age <= 0) {
            System.err.println("Age must be positive.");
            return null;
        }

        return ptDAO.addPersonalTrainer(name, age);
    }

    // Get all Personal Trainers
    public List<PersonalTrainer> getAllPersonalTrainers() {
        return ptDAO.getAllPersonalTrainers();
    }

    // Get a specific Personal Trainer by ID
    public PersonalTrainer getPersonalTrainerById(int ptId) {
        if (ptId <= 0) {
            System.err.println("Invalid Personal Trainer ID.");
            return null;
        }

        return ptDAO.getPersonalTrainerById(ptId);
    }

    // Get workout plans created by a specific PT
    public List<WorkoutPlan> getWorkoutPlansByPT(int ptId) {
        if (ptId <= 0) {
            System.err.println("Invalid Personal Trainer ID.");
            return null;
        }

        return ptDAO.getPlansMadeByPT(ptId);
    }

    // Get users trained by a PT
    public List<User> getUsersTrainedByPT(int ptId) {
        if (ptId <= 0) {
            System.err.println("Invalid Personal Trainer ID.");
            return null;
        }

        return ptDAO.getUsersOfPT(ptId);
    }

    // Update Personal Trainer info
    public void updatePersonalTrainer(int ptId, String newName, int newAge) {
        if (ptId <= 0 || newName == null || newName.trim().isEmpty() || newAge <= 0) {
            System.err.println("Invalid input for updating Personal Trainer.");
            return;
        }

        ptDAO.editPersonalTrainer(ptId, newName, newAge);
    }

    // Delete Personal Trainer
    public void deletePersonalTrainer(int ptId) {
        if (ptId <= 0) {
            System.err.println("Invalid Personal Trainer ID.");
            return;
        }

        ptDAO.deletePersonalTrainer(ptId);
    }
}
