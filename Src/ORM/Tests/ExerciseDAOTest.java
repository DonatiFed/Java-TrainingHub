package ORM.Tests;

import ORM.ExerciseDAO;
import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.ExerciseStrategyFactory;

import java.util.List;

public class ExerciseDAOTest {
    public static void main(String[] args) {
        ExerciseDAO exerciseDAO = new ExerciseDAO();

        //  CREATE
        System.out.println(" Adding Exercise...");
        exerciseDAO.addExercise("Bench Press", "Chest exercise", "Barbell", 3, 10, 80, "strength");

        //  READ
        List<Exercise> exercises = exerciseDAO.getAllExercises();
        int x=0;
        for (Exercise exercise : exercises) {
            x++;
        }
        System.out.println("x:"+x);


        //  UPDATE
        System.out.println(" Updating Exercise...");
        exerciseDAO.updateExercise(1, "Bench Press", "Updated Desc", "Barbell", 5, 12, 90, "hypertrophy");

        //  DELETE
        System.out.println(" Deleting Exercise...");
        exerciseDAO.deleteExercise(3);

        //  Verify Deletion
        exercises = exerciseDAO.getAllExercises();
        x=0;
        for (Exercise exercise : exercises) {
            x++;
        }
        System.out.println("x:"+x);
    }
}

