package Model.WorkoutManagement;

import java.util.List;

public abstract class Workout {
    protected int id;
    public abstract void AddExercise(Exercise exercise);
    public abstract void RemoveExercise(Exercise exercise);

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
