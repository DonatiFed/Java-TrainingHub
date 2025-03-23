package ORM.Tests;


import Model.WorkoutManagement.Exercise;
import ORM.Workout4PlanDAO;
import Model.WorkoutManagement.Workout4Plan;

import java.util.List;

public class Workout4PlanDAOTest { // Se la strategy inserita non è delle 3 giuste, si rompe il readAll!!
    public static void main(String[] args) {
        Workout4PlanDAO workoutDAO = new Workout4PlanDAO();

        //  CREATE
        System.out.println(" Adding Workout4Plan...");
        Workout4PlanDAO workout4PlanDAO = new Workout4PlanDAO();


        Workout4Plan newWorkout = workout4PlanDAO.addWorkout4Plan("Monday", "Strength");
        //  DELETE
        System.out.println(" Deleting Workout4Plan...");
        workoutDAO.deleteWorkout4Plan(25);

        //  READ
        List<Workout4Plan> workouts = workoutDAO.getAllWorkout4Plans();
        System.out.println(" Retrieved Workouts: " + workouts);
        int x=0;
        for(Workout4Plan workout: workouts){
            x++;
        }
        System.out.println("x:"+x);

        //  UPDATE
        System.out.println(" Updating Workout4Plan...");
        workoutDAO.updateWorkout4Plan(1, "Tuesday", "hypertrophy");

        //ADD workout
        workout4PlanDAO.addExerciseToWorkout4Plan(1,5);
        workout4PlanDAO.addExerciseToWorkout4Plan(2,5);
        workout4PlanDAO.addExerciseToWorkout4Plan(1,6);

        //  Verify Deletion
        workouts = workoutDAO.getAllWorkout4Plans();
        System.out.println(" Remaining Workouts: " + workouts);
    }
}

