package Model.WorkoutManagement;

public class ExerciseStrategyFactory {
    public static ExerciseIntensitySetter createStrategy(String type) {
        switch (type.toLowerCase()) {
            case "strength":
                return new StrengthExercise();
            case "hypertrophy":
                return new HypertrophyExercise();
            case "endurance":
                return new EnduranceExercise();
            default:
                throw new IllegalArgumentException("Unknown strategy type: " + type);
        }
    }
}