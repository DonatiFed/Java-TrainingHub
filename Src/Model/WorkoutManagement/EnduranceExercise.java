package Model.WorkoutManagement;


public class EnduranceExercise implements ExerciseIntensitySetter {
    @Override
    public int setNSets() {
        return 3; //  3 sets for endurance
    }

    @Override
    public int setNReps() {
        return 15; // 15+ reps for endurance
    }
}
