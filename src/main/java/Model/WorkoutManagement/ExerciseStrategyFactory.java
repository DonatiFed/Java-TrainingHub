package Model.WorkoutManagement;

public class ExerciseStrategyFactory {
    public static ExerciseIntensitySetter createStrategy(String type) {
        switch (type.toLowerCase()) {
            case "strength":
                return new StrengthExerciseSetter();
            case "hypertrophy":
                return new HypertrophyExerciseSetter();
            case "endurance":
                return new EnduranceExerciseSetter();
            default:
                throw new IllegalArgumentException("Unknown strategy type: " + type);
        }
    }
}

