package Model.WorkoutManagement;

import Model.UserManagement.Observer;
import Model.UserManagement.User;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlan extends WorkoutSubject{
    private String lastEditDate;
    private List<Workout4Plan> workouts;


    //IN WORKOUT PLAN THERE WILL BE ONLY OBSERVERS OF PEOPLE TO WHOM THE WORKOUT PLAN IS DEDICATED(WHETHER IT'S A TRAINED OR ANOTHER PT)
    public WorkoutPlan(Observer trainedUser,int id){
        super();
        this.lastEditDate = java.time.LocalDate.now().toString();
        this.attach(trainedUser);    //I don't need the attribute "trained User", but that is the only observer I will attach
        workouts=new ArrayList<>();
        this.id=id;
    }
    public void addWorkout(Workout4Plan workout){
        workouts.add(workout);
        this.lastEditDate = java.time.LocalDate.now().toString();
        notifyObservers();
    }
    public void removeWorkout(Workout4Plan workout) {
        boolean isPresent = false;
        for (Workout4Plan wp : this.workouts) {
            if (wp.getId() == workout.getId()) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            System.out.println("Workout not found.");
        } else {
            this.workouts.remove(workout);
            this.lastEditDate = java.time.LocalDate.now().toString();
            System.out.println("Workout successfully removed.");
            notifyObservers();
        }
    }
    public void editWorkout(Workout4Plan workout, int newId,String newDay,ExerciseIntensitySetter newStrategy) {
        boolean isPresent = false;
        for (Workout4Plan wp : this.workouts) {
            if (wp.getId() == workout.getId()) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            System.out.println("Workout not found.");
        } else {
            workout.setId(newId);
            workout.setDay(newDay);
            workout.setStrategy(newStrategy);
            this.lastEditDate = java.time.LocalDate.now().toString();
            System.out.println("Workout successfully edited.");
            notifyObservers();
        }
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

    public void setWorkouts(List<Workout4Plan> workouts) {
        this.workouts = workouts;
    }
}
