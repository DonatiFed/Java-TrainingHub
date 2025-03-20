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

    @Override
    public void notifyObservers() {
        for(Observer pt:observers){
            pt.update("WorkoutRecordUpdate"); //I expect to be notified all the user's pts
        }
    }

    public String getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(String lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    public int getnWorkouts() {
        return nWorkouts;
    }

    public void setnWorkouts(int nWorkouts) {
        this.nWorkouts = nWorkouts;
    }

    public List<Workout4Record> getWorkouts() {
        return workouts;
    }

}
