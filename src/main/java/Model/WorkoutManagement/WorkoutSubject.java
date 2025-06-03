package Model.WorkoutManagement;

import Model.UserManagement.Observer;

import java.util.ArrayList;
import java.util.List;

public abstract class WorkoutSubject {
    protected List<Observer> observers;
    protected int id;

    public WorkoutSubject(){
        observers=new ArrayList<>();
    }
    // add observer
    public void attach(Observer observer) {
        observers.add(observer);
    }

    // remove observer
    public void detach(Observer observer) {
        observers.remove(observer);
    }



    // to be implemented in concrete class
    public abstract void notifyObservers();

    public List<Observer> getObservers() {
        return observers;
    }

    public void setObservers(List<Observer> observers) {
        this.observers = observers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}




