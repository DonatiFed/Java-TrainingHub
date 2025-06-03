package Model.UserManagement;

import java.util.ArrayList;
import java.util.List;

public class Trainee extends User{
    public Trainee(int id, String name, int age) {
        super(id, name, age);
        this.personaltrainers=new ArrayList<>();
    }

    @Override
    public void update(String context) {

    }


}
