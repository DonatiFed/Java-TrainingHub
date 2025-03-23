package ORM.Tests;

import Model.UserManagement.Trainee;
import ORM.WorkoutRecordDAO;
import Model.WorkoutManagement.WorkoutRecord;

import java.util.List;

public class WorkoutRecordDAOTest {
    public static void main(String[] args) {
        WorkoutRecordDAO workoutRecordDAO = new WorkoutRecordDAO();
        Trainee trainee = new Trainee(5,"Mago",20);

        //  CREATE
        System.out.println(" Adding WorkoutRecord...");
        workoutRecordDAO.addWorkoutRecord();

        //  READ
        List<WorkoutRecord> records = workoutRecordDAO.getAllWorkoutRecords();
        System.out.println(" Retrieved WorkoutRecords: " + records);

        //  DELETE
        System.out.println(" Deleting WorkoutRecord...");
        workoutRecordDAO.deleteWorkoutRecord(1);

        //  Verify Deletion
        records = workoutRecordDAO.getAllWorkoutRecords();
        System.out.println(" Remaining WorkoutRecords: " + records);
    }
}

