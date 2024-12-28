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
    public void CreateWorkoutPlan(User user,int id){
        workoutPlans.add(new WorkoutPlan(user,id));
    }
    public void EditWorkoutPlan(WorkoutPlan wp,int newId){  //I can't think of a better edit of the workout plan without breaking the SRP...for better
        boolean isPresent=false;
        for(WorkoutPlan wop:workoutPlans){//plan editing we must implement the logic in the BusinessLogic package based on the pt's input
            if (wop.getId()==wp.getId()){
                isPresent=true;
            }
        }
        if(!isPresent){
            System.out.println("WorkoutPlan not found.");
        }
        else{
            wp.setId(newId);
            System.out.println("WorkoutPlan successfully updated.");
        }

    }
    public void DeleteWorkoutPlan(WorkoutPlan wp){
        boolean isPresent=false;
        for(WorkoutPlan wop:workoutPlans){//plan editing we must implement the logic in the BusinessLogic package based on the pt's input
            if (wop.getId()==wp.getId()){
                isPresent=true;
                break;
            }
        }
        if(!isPresent){
            System.out.println("WorkoutPlan not found.");
        }
        else{
            workoutPlans.remove(wp);
            System.out.println("WorkoutPlan successfully removed.");
        }
    }
    public void AddUser(User user){
        boolean alreadyPresent=false;
        for(User u:followedusers){
            if(u.getId()==user.getId()){
                alreadyPresent=true;
                break;
            }
        }
        if(alreadyPresent){
            System.out.println("User already added");
        }
        else{
            followedusers.add(user);
            System.out.println("User successfully added");
        }
    }
    public void RemoveUser(User user){
        boolean isPresent=false;
        for(User u:followedusers){
            if(u.getId()==user.getId()){
                isPresent=true;
                break;
            }
        }
        if(!isPresent){
            System.out.println("User not found.");
        }
        else{
            followedusers.remove(user);
            System.out.println("User successfully removed");
        }
    }
    public void AddPersonalTrainer(PersonalTrainer pt){
        boolean alreadyPresent=false;
        for(PersonalTrainer pers:personaltrainers){
            if(pt.getId()==pers.getId()){
                alreadyPresent=true;
                break;
            }
        }
        if(alreadyPresent){
            System.out.println("Personal trainer already added");
        }
        else{
            followedusers.add(pt);
            System.out.println("Personal Trainer successfully added");
        }
    }
    public void RemovePersonaltrainer(PersonalTrainer pt){
        boolean isPresent=false;
        for(PersonalTrainer pers:personaltrainers){
            if(pers.getId()==pt.getId()){
                isPresent=true;
                break;
            }
        }
        if(!isPresent){
            System.out.println("Personal Trainer not found.");
        }
        else{
            followedusers.remove(pt);
            System.out.println("Personal trainer successfully removed");
        }
    }
    public void ViewuserWorkoutRecord(User u){
        boolean isPresent=false;
        for(User us:followedusers){
            if(u.getId()==us.getId()){
                isPresent=true;
                break;
            }
        }
        if(!isPresent){
            System.out.println("User not found.");
        }
        else{
            System.out.println(u.getName()+"'s WorkoutRecord: "+u.getWorkoutrecord());
        }
    }



    @Override
    public void update(String context) {
        if(context=="WorkoutPlanUpdate"){
            System.out.println("Workout plan updated");  //one of the pt's pts has updated his workout plan
        }
        else{
            System.out.println("Workout record updated"); //one of the pt's trained has updated his workout record
        }
    }
}
