package Model.WorkoutManagement;

public class HypertrophyExerciseSetter implements ExerciseIntensitySetter {
    @Override
    public int setNSets() {
        return 4; // 4 sets for hypertrophy
    }

    @Override
    public int setNReps() {
        return 8; // 8-12 reps for hypertrophy
    }

    @Override
    public String toString() {
        return "Hypertrophy";
    }
}