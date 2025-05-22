package Model.WorkoutManagement;

public class StrengthExerciseSetter implements ExerciseIntensitySetter {
    @Override
    public int setNSets() {
        return 5; // 5 sets for strength
    }

    @Override
    public int setNReps() {
        return 3; // 3 reps for strength
    }
}
