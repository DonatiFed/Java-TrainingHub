package Model.WorkoutManagement;

public class Workout4PlanFactory implements WorkoutFactory {
    private String day;
    private String strategy;
    private int id;

    public Workout4PlanFactory(String day, String strategy,int id) {
        this.day = day;
        this.strategy = strategy;
        this.id=id;
    }

    @Override
    public Workout4Plan createWorkout() {
        return new Workout4Plan(day, strategy,id);
    }
}
