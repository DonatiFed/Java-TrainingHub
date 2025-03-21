package ORM.Tests;

import Model.UserManagement.Trainee;
import ORM.TraineeDAO;
import ORM.WorkoutRecordDAO;
import Model.WorkoutManagement.WorkoutRecord;

import java.util.List;

public class WorkoutRecordDAOTest {
    public static void main(String[] args) {
        WorkoutRecordDAO workoutRecordDAO = new WorkoutRecordDAO();

        //  CREATE
        TraineeDAO traineeDAO = new TraineeDAO();
        Trainee trainee = new Trainee(5,"luca",5);
        System.out.println(" Adding WorkoutRecord...");
        workoutRecordDAO.addWorkoutRecord(trainee);

        //  READ
        List<WorkoutRecord> records = workoutRecordDAO.getAllWorkoutRecords();    //TODO: cast date data type from date to string
        for (WorkoutRecord record : records) {
            System.out.println(record.toString());
        }
        System.out.println(" Retrieved WorkoutRecords: " + records.get(0).getId());

        //  DELETE
        System.out.println(" Deleting WorkoutRecord...");
        workoutRecordDAO.deleteWorkoutRecord(1);

        //  Verify Deletion
        records = workoutRecordDAO.getAllWorkoutRecords();
        System.out.println(" Remaining WorkoutRecords: " + records);
    }
}