package Model.UserManagement;

import Model.WorkoutManagement.Workout4Plan;
import Model.WorkoutManagement.WorkoutPlan;

import java.util.ArrayList;
import java.util.List;

public class PersonalTrainer extends User{
    private List<User> followedusers;
    private List<WorkoutPlan> workoutPlans;

    public PersonalTrainer(int id, String name, int age) {
        super(id, name, age);
        personaltrainers=new ArrayList<>();
        followedusers=new ArrayList<>();
    }

    @Override
    public void update(String context) {
    }
}
