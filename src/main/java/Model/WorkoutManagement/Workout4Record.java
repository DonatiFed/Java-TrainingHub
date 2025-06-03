package Model.WorkoutManagement;
import java.util.ArrayList;
import java.util.List;


public class Workout4Record extends Workout{
    private List<Exercise> exercises;
    private String date;
    public Workout4Record(String date, int id){
        this.date = date;
        exercises=new ArrayList<>();
        this.id=id;
    }

    //An exercise created with the Constructor variant without Strategy will be added
    public void AddExercise(Exercise exercise) {  //I can only add one exercise with the same amount of res for each set...I have to change the name of the exercise in,

    }
    public void RemoveExercise(Exercise exercise){

    }
    public void EditExercise(Exercise exercise,String newName,String newEquipment,int newN_Sets,int newN_Reps,int newWeight){

    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
}


