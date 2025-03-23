package ORM.Tests;

import ORM.PersonalTrainerDAO;
import Model.UserManagement.PersonalTrainer;

import java.util.List;

public class PersonalTrainerDAOTest {
    public static void main(String[] args) {
        PersonalTrainerDAO ptDAO = new PersonalTrainerDAO();

        //  CREATE
        System.out.println(" Adding Personal Trainer...");
        ptDAO.addPersonalTrainer("John Doe", 30);

        //  READ
        List<PersonalTrainer> trainers = ptDAO.getAllPersonalTrainers();
        System.out.println(" Retrieved PTs: " + trainers);

        //  UPDATE
        System.out.println(" Updating Personal Trainer...");
        ptDAO.editPersonalTrainer(1, "John Updated", 35);

        //  DELETE
        System.out.println(" Deleting Personal Trainer...");
        ptDAO.deletePersonalTrainer(1);

        //  Verify Deletion
        trainers = ptDAO.getAllPersonalTrainers();
        System.out.println(" Remaining PTs: " + trainers);
    }
}
