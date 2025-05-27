package BusinessLogic;

import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.User;
import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.WorkoutRecord;
import ORM.PersonalTrainerDAO;
import ORM.WorkoutPlanDAO;
import ORM.WorkoutRecordDAO;

import java.util.List;

public class PersonalTrainerController {
    private final PersonalTrainerDAO ptDAO;
    private final WorkoutPlanDAO wpDAO;
    private final WorkoutRecordDAO wrDAO;

    // Constructor injection of DAOs — must be created elsewhere and passed in
    public PersonalTrainerController(PersonalTrainerDAO ptDAO, WorkoutPlanDAO wpDAO, WorkoutRecordDAO wrDAO) {
        this.ptDAO = ptDAO;
        this.wpDAO = wpDAO;
        this.wrDAO = wrDAO;
    }

    // Register PT and create linked WorkoutRecord
    public PersonalTrainer registerPersonalTrainer(String name, int age) {
        PersonalTrainer pt = ptDAO.addPersonalTrainer(name, age);
        if (pt == null) {
            System.out.println("❌ Failed to create Personal Trainer.");
            return null;
        }

        var wr = wrDAO.addWorkoutRecord();
        if (wr == null) {
            System.out.println("❌ Failed to create WorkoutRecord.");
            return pt; // Returning pt even if workout record creation fails
        }

        try {
            wrDAO.linkWorkoutRecordToUser(pt.getId(), wr.getId());
            System.out.println("✅ Created Personal Trainer (ID: " + pt.getId() + ") with linked WorkoutRecord (ID: " + wr.getId() + ")");
        } catch (Exception e) {
            System.out.println("❌ Failed to link WorkoutRecord to Personal Trainer.");
            e.printStackTrace();
        }
        return pt;
    }

    // Assign WorkoutPlan to Trainee by this PT
    public void followTrainee(int ptId, int traineeId, int workoutPlanId) {
        wpDAO.assignWorkoutPlanToUser(workoutPlanId, traineeId, ptId);
        System.out.println("✅ Assigned WorkoutPlan " + workoutPlanId + " to Trainee " + traineeId + " by PT " + ptId);
    }

    public List<PersonalTrainer> getAllPersonalTrainers() {
        return ptDAO.getAllPersonalTrainers();
    }

    public PersonalTrainer getPersonalTrainerById(int ptId) {
        return ptDAO.getPersonalTrainerById(ptId);
    }

    public void updatePersonalTrainer(int ptId, String newName, int newAge) {
        ptDAO.editPersonalTrainer(ptId, newName, newAge);
        System.out.println("✅ Updated Personal Trainer with ID: " + ptId);
    }

    public void deletePersonalTrainer(int ptId) {
        ptDAO.deletePersonalTrainer(ptId);
        System.out.println("✅ Deleted Personal Trainer with ID: " + ptId);
    }

    public List<User> getTraineesOfPT(int ptId) {
        return ptDAO.getTraineesOfPT(ptId);
    }

    public List<WorkoutPlan> getPlansMadeByPT(int ptId) {
        return ptDAO.getPlansMadeByPT(ptId);
    }

    // New method: fetch workout record of a trainee only if the PT follows the trainee
    public WorkoutRecord getWorkoutRecordForTrainee(int ptId, int traineeId) {
        // Check if ptId follows traineeId (make sure you have this method in your DAO)
        if (!ptDAO.isFollowing(ptId, traineeId)) {
            System.err.println("Personal Trainer with ID " + ptId + " does not follow Trainee with ID " + traineeId);
            return null;
        }
        // Return the workout record linked to the trainee
        return wrDAO.getWorkoutRecordByUserId(traineeId);
    }
}
