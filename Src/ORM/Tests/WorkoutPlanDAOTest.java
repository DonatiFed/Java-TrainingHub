package ORM.Tests;

import Model.UserManagement.Trainee;
import Model.WorkoutManagement.Workout4Plan;
import ORM.WorkoutPlanDAO;
import Model.WorkoutManagement.WorkoutPlan;

import java.util.List;

public class WorkoutPlanDAOTest {
    public static void main(String[] args) {
        WorkoutPlanDAO workoutPlanDAO = new WorkoutPlanDAO();

        Trainee trainee=new Trainee(5,"Timo",21);

        //  CREATE
        System.out.println(" Adding WorkoutPlan...");
        //workoutPlanDAO.addWorkoutPlan(trainee);

        //  READ
        List<WorkoutPlan> plans = workoutPlanDAO.getAllWorkoutPlans();
        System.out.println(" Retrieved WorkoutPlans: " + plans);

        //  DELETE
        //System.out.println(" Deleting WorkoutPlan...");
        //workoutPlanDAO.deleteWorkoutPlan(2);

        //ADD WORKOUT
        workoutPlanDAO.addWorkout4PlanToWorkoutPlan(7,6);
        workoutPlanDAO.addWorkout4PlanToWorkoutPlan(7,7);
        workoutPlanDAO.addWorkout4PlanToWorkoutPlan(9,8);
        workoutPlanDAO.addWorkout4PlanToWorkoutPlan(9,9);
        workoutPlanDAO.addWorkout4PlanToWorkoutPlan(9,10);

        //  Verify Deletion
        plans = workoutPlanDAO.getAllWorkoutPlans();
        for (WorkoutPlan plan : plans) {
            List<Workout4Plan> w4ps = plan.getWorkouts();
            for(Workout4Plan w4p:w4ps ){
                System.out.println("workoutPlanId: "+plan.getId()+",workout id: "+w4p.getId()+",workout strategy"+w4p.getStrategy().toString());
            }
        }
    }
}

