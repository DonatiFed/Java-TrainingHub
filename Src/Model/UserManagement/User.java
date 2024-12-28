package Model.UserManagement;
import java.util.ArrayList;
import java.util.List;



import Model.WorkoutManagement.Workout4Plan;
import Model.WorkoutManagement.Workout4Record;

import java.util.List;

public abstract class User extends Observer{
    protected int id;
    protected String name;
    protected int age;
    protected List<Workout4Record> workoutrecord;   //For the moment I preferred to leave only one Record for each usr, but then we can implement a solution with many records
    protected List<PersonalTrainer> personaltrainers;

    public List<PersonalTrainer> getPersonaltrainers() {
        return personaltrainers;
    }

    public void setPersonaltrainers(List<PersonalTrainer> personaltrainers) {
        this.personaltrainers = personaltrainers;
    }

    public User(int id, String name, int age){
        this.id = id;
        this.name = name;
        this.age = age;
        this.workoutrecord = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }



    public List<Workout4Record> getWorkoutrecord() {
        return workoutrecord;
    }

    public void setWorkoutrecord(List<Workout4Record> workoutrecord) {
        this.workoutrecord = workoutrecord;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
