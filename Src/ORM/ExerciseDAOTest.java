package ORM;

import java.sql.SQLException;

public class ExerciseDAOTest {
    public static void main(String[] args) throws SQLException {
        ExerciseDAO dao = new ExerciseDAO();

        // CREATE
        //dao.addExercise("Push-up", "Bodyweight exercise", "None", 3, 15, 0, "Strength");
        //dao.addExercise("Squat", "Leg exercise", "None", 3, 12, 0, "Endurance");

        // READ
        System.out.println("All Exercises: " + dao.getAllExercises());

        // UPDATE
        //dao.updateExercise(1, "Push-up", "Updated description", "None", 4, 20, 0, "Strength");

        // DELETE
        //dao.deleteExercise(2);
    }
}

