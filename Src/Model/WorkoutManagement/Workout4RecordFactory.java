package Model.WorkoutManagement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Workout4RecordFactory implements WorkoutFactory {
    private String date;
    private int id;
    public Workout4RecordFactory(String date,int id) {
        this.date = date;
        this.id=id;
    }

    @Override
    public Workout4Record createWorkout() {
        return new Workout4Record(date,id);
    }
}
