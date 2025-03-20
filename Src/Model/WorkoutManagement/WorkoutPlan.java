package Model.WorkoutManagement;

import Model.UserManagement.Observer;
import Model.UserManagement.User;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlan extends WorkoutSubject{
    private String lastEditDate;
    private List<Workout4Plan> workouts;


    //IN WORKOUT PLAN THERE WILL BE ONLY OBSERVERS OF PEOPLE TO WHOM THE WORKOUT PLAN IS DEDICATED(WHETHER IT'S A TRAINED OR ANOTHER PT)
    public WorkoutPlan(Observer traineeUser,int id){
        super();
        this.lastEditDate = java.time.LocalDate.now().toString();
        this.attach(traineeUser);    //I don't need the attribute "trainee User", but that is the only observer I will attach
        workouts=new ArrayList<>();
        this.id=id;
    }

    // Only notify the observer trainedUser
    @Override
    public void notifyObservers() {
        for(Observer trainedUser:observers) {
            trainedUser.update("WorkoutPlanUpdate");          //I expect only one observer to be notified
        }
    }

    public String getLastEditDate() {
        return lastEditDate;
    }
    public void setLastEditDate(String lastEditDate) {
        this.lastEditDate = lastEditDate;
    }
    public List<Workout4Plan> getWorkouts() {
        return workouts;
    }
}


