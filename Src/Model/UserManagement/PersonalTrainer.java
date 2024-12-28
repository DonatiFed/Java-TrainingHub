package Model.UserManagement;

import java.util.ArrayList;
import java.util.List;

public class PersonalTrainer extends User{
    private List<PersonalTrainer> personaltrainers;
    private List<PersonalTrainer> followedusers;

    public PersonalTrainer(int id, String name, int age) {
        super(id, name, age);
        personaltrainers=new ArrayList<>();
        followedusers=new ArrayList<>();
    }
    public void CreateWorkoutPlan(){

    }
    public void EditWorkoutPlan(){

    }
    public void DeleteWorkoutPlan(){

    }
    public void AddUser(){

    }
    public void RemoveUser(){

    }
    public void AddPersonalTrainer(){

    }
    public void RemovePersonaltrainer(){

    }
    public void ViewuserWorkoutRecord(){

    }



    @Override
    public void update() {

    }
}
