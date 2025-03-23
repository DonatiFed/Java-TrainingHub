package ORM.Tests;

import ORM.TraineeDAO;
import ORM.WorkoutRecordDAO;
import Model.UserManagement.Trainee;
import Model.WorkoutManagement.WorkoutPlan;
import Model.UserManagement.PersonalTrainer;

import java.util.List;

public class TraineeDAOTest {
    public static void main(String[] args) {
        TraineeDAO traineeDAO = new TraineeDAO();
        WorkoutRecordDAO workoutRecordDAO = new WorkoutRecordDAO();

        //  Test Adding a Trainee
        System.out.println("Creating a new Trainee...");
        Trainee trainee = traineeDAO.addTrainee("John Doe", 25);
        if (trainee != null) {
            System.out.println("Trainee Created: ID=" + trainee.getId() + ", Name=" + trainee.getName() + ", Age=" + trainee.getAge());
        } else {
            System.out.println(" Failed to create Trainee.");
        }

        //  Test Retrieving All Trainees
        System.out.println("\nFetching all trainees...");
        List<Trainee> trainees = traineeDAO.getAllTrainees();
        for (Trainee t : trainees) {
            System.out.println("Trainee ID: " + t.getId() + ", Name: " + t.getName() + ", Age: " + t.getAge());
        }

        //  Test Retrieving a Trainee's Workout Plan
        System.out.println("\nFetching Workout Plans for Trainee ID=" + trainee.getId());
        List<WorkoutPlan> workoutPlans = traineeDAO.getWorkoutPlanFromUserId(trainee.getId());
        if (workoutPlans.isEmpty()) {
            System.out.println(" No Workout Plans found.");
        } else {
            for (WorkoutPlan plan : workoutPlans) {
                System.out.println("Workout Plan ID: " + plan.getId() + ", Last Edit Date: " + plan.getLastEditDate());
            }
        }

        //  Test Retrieving a Trainee's Assigned PT
        System.out.println("\nFetching Personal Trainer for Trainee ID=" + trainee.getId());
        PersonalTrainer pt = traineeDAO.getPTForUserId(trainee.getId());
        if (pt != null) {
            System.out.println(" Assigned PT: ID=" + pt.getId() + ", Name=" + pt.getName() + ", Age=" + pt.getAge());
        } else {
            System.out.println(" No PT assigned to this Trainee.");
        }

        //  Test Editing a Trainee
        System.out.println("\nEditing Trainee...");
        traineeDAO.editUser(trainee.getId(), "Johnathan Doe", 26);
        Trainee updatedTrainee = traineeDAO.getAllTrainees().stream()
                .filter(t -> t.getId() == trainee.getId()).findFirst().orElse(null);
        if (updatedTrainee != null) {
            System.out.println(" Trainee Updated: ID=" + updatedTrainee.getId() + ", Name=" + updatedTrainee.getName() + ", Age=" + updatedTrainee.getAge());
        } else {
            System.out.println(" Failed to update Trainee.");
        }

        //  Test Deleting a Trainee
        System.out.println("\nDeleting Trainee ID=" + trainee.getId());
        traineeDAO.deleteUser(trainee.getId());
        Trainee deletedTrainee = traineeDAO.getAllTrainees().stream()
                .filter(t -> t.getId() == trainee.getId()).findFirst().orElse(null);
        if (deletedTrainee == null) {
            System.out.println(" Trainee successfully deleted.");
        } else {
            System.out.println("Trainee deletion failed.");
        }
    }
}

