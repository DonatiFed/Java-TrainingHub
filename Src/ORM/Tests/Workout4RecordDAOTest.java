package ORM.Tests;


import Model.WorkoutManagement.Exercise;
import Model.WorkoutManagement.Workout4Record;
import ORM.Workout4PlanDAO;
import ORM.Workout4RecordDAO;

import java.util.List;

public class Workout4RecordDAOTest {
    public static void main(String[] args) {
        Workout4RecordDAO dao = new Workout4RecordDAO();

        // 1. Test Adding a Workout4Record
        System.out.println("=== Adding a new Workout4Record ===");
        Workout4Record newRecord = dao.addWorkout4Record("2025-03-22");
        if (newRecord != null) {
            System.out.println("Workout4Record added: ID = " + newRecord.getId() + ", Date = " + newRecord.getDate());
        } else {
            System.out.println("Failed to add Workout4Record.");
        }

        // 2. Test Fetching All Workout4Records
        System.out.println("\n=== Fetching All Workout4Records ===");
        for (Workout4Record record : dao.getAllWorkout4Records()) {
            System.out.println("ID: " + record.getId() + ", Date: " + record.getDate());
        }

        // 3. Test Fetching a Workout4Record by ID
        if (newRecord != null) {
            System.out.println("\n=== Fetching Workout4Record by ID ===");
            Workout4Record fetchedRecord = dao.getWorkout4RecordById(newRecord.getId());
            if (fetchedRecord != null) {
                System.out.println("Fetched Record - ID: " + fetchedRecord.getId() + ", Date: " + fetchedRecord.getDate());
            } else {
                System.out.println("No record found.");
            }
        }

        // 4. Test Updating a Workout4Record
        if (newRecord != null) {
            System.out.println("\n=== Updating Workout4Record Date ===");
            dao.updateWorkout4RecordDate(newRecord.getId(), "2025-04-01");
            Workout4Record updatedRecord = dao.getWorkout4RecordById(newRecord.getId());
            System.out.println("Updated Record - ID: " + updatedRecord.getId() + ", Date: " + updatedRecord.getDate());
        }

        // 5. Test Deleting a Workout4Record
        if (newRecord != null) {
            System.out.println("\n=== Deleting Workout4Record ===");
            dao.deleteWorkout4Record(newRecord.getId());
            Workout4Record deletedRecord = dao.getWorkout4RecordById(newRecord.getId());
            if (deletedRecord == null) {
                System.out.println("Record successfully deleted.");
            } else {
                System.out.println("Failed to delete record.");
            }
        }
    }
}

