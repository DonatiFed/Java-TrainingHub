package Model.WorkoutManagement;

import Model.UserManagement.Observer;
import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.User;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRecord extends WorkoutSubject{
    private String lastEditDate;
    private int nWorkouts;
    private List<Workout4Record> workouts;
    private User user;




    //IN WORKOUT RECORD THERE WILL BE ONLY OBSERVERS OF THE USER'S PT
    public WorkoutRecord(User user,int id){
        super();
        this.lastEditDate=java.time.LocalDate.now().toString();
        this.nWorkouts=0;
        this.user=user;
        this.workouts=new ArrayList<>();
        this.id=id;
        for(PersonalTrainer pt:user.getPersonaltrainers()){
            this.attach(pt); //I don't need the attribute User's pts, since they are the observers that I attach...and I attach only them
        }

    }
    public void addWorkout(Workout4Record workout){ //workout object passed here, otherwise we could create a new one while putting in the record;
        workouts.add(workout);
        this.lastEditDate=java.time.LocalDate.now().toString();
        notifyObservers();
    }
    public void removeWorkout(Workout4Record workout) {
        boolean isPresent = false;
        for (Workout4Record wr : this.workouts) {
            if (wr.getId() == workout.getId()) {
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
    public void editWorkout(Workout4Record workout, int newId,String newDate) {
        boolean isPresent = false;
        for (Workout4Record wr : this.workouts) {
            if (wr.getId() == workout.getId()) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            System.out.println("Workout not found.");
        } else {
            workout.setId(newId);
            workout.setDate(newDate);
            this.lastEditDate = java.time.LocalDate.now().toString();
            System.out.println("Workout successfully edited.");
            notifyObservers();
        }
    }
    @Override
    public void notifyObservers() {
        for(Observer pt:observers){
            pt.update("WorkoutRecordUpdate"); //I expect to be notified all the user's pts
        }
    }
}
