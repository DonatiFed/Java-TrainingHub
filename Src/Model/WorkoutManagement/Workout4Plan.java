package Model.WorkoutManagement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Workout4Plan extends Workout{
    private String day;
    private List<Exercise> exercises;
    ExerciseIntensitySetter strategy; //The strategy is the same for each exercise of the workoutplan; it determines the amount of sets and the amount of reps for each exercise

    public Workout4Plan(String day,String exerciseintensitysetter,int id){
        this.id=id;
        this.day=day;
        this.strategy=ExerciseStrategyFactory.createStrategy(exerciseintensitysetter);
        this.exercises=new ArrayList<>();
    }

    public void AddExercise(Exercise exercise) {
        boolean alreadyPresent = false;
        for (Exercise ex : this.exercises) {
            if (ex.getName() == exercise.getName()) {
                alreadyPresent = true;
                break;
            }
        }
        if (!alreadyPresent) {
            exercise.setStrategy(this.strategy);
            this.exercises.add(exercise);
            System.out.println("Exercise successfully added.");
        } else {
            System.out.println("Exercise already added.");
        }
    }
    public void RemoveExercise(Exercise exercise){ //we remove passing an object to the function...if we want the user to only write the name of the exercise or to select the exercise i the window,
        boolean isPresent = false;                 //that's something to be determined in the InterfacciaCLI package or BuisnessLogic package
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

    /* RemoveExercise variant using Iterator pattern(using Java's library implementation):

    public void RemoveExercise(Exercise exercise) {
        Iterator<Exercise> iterator = this.exercises.iterator();
        boolean isPresent = false;

        while (iterator.hasNext()) {
            Exercise ex = iterator.next();
            if (ex.getName().equals(exercise.getName())) {
                iterator.remove();
                isPresent = true;
                break;
            }
        }

        if (!isPresent) {
            System.out.println("Exercise not found.");
        } else {
            System.out.println("Exercise successfully removed.");
        }
    }*/

    public void EditExercise(Exercise exercise,String newName,String newDescription,String newEquipment,int newN_Sets,int newN_Reps,int newWeight){ //I can change the amount of sets and reps after the creation
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
            exercise.setDescription(newDescription);
            exercise.setEquipment(newEquipment);
            exercise.setN_sets(newN_Sets);
            exercise.setN_reps(newN_Reps);
            exercise.setWeight(newWeight);
            System.out.println("Exercise successfully edited.");
        }
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
