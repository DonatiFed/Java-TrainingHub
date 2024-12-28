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
        boolean alreadyPresent = false;           //for example, "bench press first set", "bench press second set", if they have different amount of reps.
        for (Exercise ex : this.exercises) {
            if (ex.getName() == exercise.getName()) {
                alreadyPresent = true;
                break;
            }
        }
        if (!alreadyPresent) {
            this.exercises.add(exercise);
            System.out.println("Exercise successfully added.");
        } else {
            System.out.println("Exercise already added.");
        }
    }
    public void RemoveExercise(Exercise exercise){
        boolean isPresent = false;
        for (Exercise ex : this.exercises) {
            if (ex.getName() == exercise.getName()) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            System.out.println("Exercise not found.");
        } else {
            this.exercises.remove(exercise);
            System.out.println("Exercise successfully removed.");
        }
    }
    public void EditExercise(Exercise exercise,String newName,String newEquipment,int newN_Sets,int newN_Reps,int newWeight){
        boolean isPresent = false;
        for (Exercise ex : this.exercises) {
            if (ex.getName() == exercise.getName()) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            System.out.println("Exercise not found.");
        } else {
            exercise.setName(newName);
            exercise.setEquipment(newEquipment);
            exercise.setN_sets(newN_Sets);
            exercise.setN_reps(newN_Reps);
            exercise.setWeight(newWeight);
            System.out.println("Exercise successfully edited.");
        }
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


