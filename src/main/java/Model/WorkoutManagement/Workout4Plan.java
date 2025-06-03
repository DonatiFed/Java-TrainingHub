package Model.WorkoutManagement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Workout4Plan extends Workout{
    private String day;
    private List<Exercise> exercises;
    ExerciseIntensitySetter strategy; //The strategy is the same for each exercise of the workoutplan; it determines the amount of sets and the amount of reps for each exercise

    public Workout4Plan(String day,String exerciseintensity,int id){
        this.id=id;
        this.day=day;
        this.strategy=ExerciseStrategyFactory.createStrategy(exerciseintensity);
        this.exercises=new ArrayList<>();
    }

    public void AddExercise(Exercise exercise) {

    }
    public void RemoveExercise(Exercise exercise){ //we remove passing an object to the function...if we want the user to only write the name of the exercise or to select the exercise i the window,

    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public ExerciseIntensitySetter getStrategy() {
        return strategy;
    }

    public void setStrategy(ExerciseIntensitySetter strategy) {
        this.strategy = strategy;
    }
}
