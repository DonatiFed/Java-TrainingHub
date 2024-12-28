package Model.UserManagement;

import Model.WorkoutManagement.Workout4Plan;

import java.util.ArrayList;
import java.util.List;

public class Trained extends User{
    private List<PersonalTrainer> personaltrainers;

    public Trained(int id, String name, int age) {
        super(id, name, age);
        personaltrainers=new ArrayList<>();
    }

    @Override
    public void update() {
        System.out.println("Personaltrainers list updated!");
    }
    private void AddPersonalTrainer(PersonalTrainer pt) {
        boolean alreadyPresent = false;
        for (PersonalTrainer trainer : this.personaltrainers) {
            if (trainer.getId() == pt.getId()) {
                alreadyPresent = true;
                break;
            }
        }
        if (!alreadyPresent) {
            this.personaltrainers.add(pt);
            System.out.println("personal trainer succesfully added.");
        } else {
            System.out.println("Personal trainer already added.");
        }
    }
    private void RemovePersonalTrainer(PersonalTrainer pt) {
        boolean isPresent = false;
        for (PersonalTrainer trainer : this.personaltrainers) {
            if (trainer.getId() == pt.getId()) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            System.out.println("personal trainer not found.");
        } else {
            personaltrainers.remove(pt);
            System.out.println("Personal trainer successfully removed.");
        }
    }

    public List<PersonalTrainer> getPersonaltrainers() {
        return personaltrainers;
    }

    public void setPersonaltrainers(List<PersonalTrainer> personaltrainers) {
        this.personaltrainers = personaltrainers;
    }
}
