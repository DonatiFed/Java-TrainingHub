package Model.UserManagement;

import Model.WorkoutManagement.Workout4Plan;

import java.util.ArrayList;
import java.util.List;

public class Trained extends User{
    public Trained(int id, String name, int age) {
        super(id, name, age);
        this.personaltrainers=new ArrayList<>();
    }

    @Override
    public void update(String context) {
        if(context=="WorkoutPlanUpdate"){                 //if is useless, because a Trained will never be notified for a WorkoutRecord Update!
            System.out.println("Workout Plan Updated!");
        }
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
            System.out.println("Personal trainer successfully added.");
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
