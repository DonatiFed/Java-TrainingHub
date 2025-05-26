package Model.UserManagement.Tests;

import Model.UserManagement.*;
import Model.WorkoutManagement.*;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagementTest {
    @Test
    void testPersonalTrainerFollowedUsers() {
        PersonalTrainer pt = new PersonalTrainer(101, "PT Name", 30);
        Trainee trainee1 = new Trainee(201, "Trainee 1", 25);
        Trainee trainee2 = new Trainee(202, "Trainee 2", 28);

        pt.getFollowedusers().add(trainee1);
        pt.setFollowedusers(List.of(trainee1, trainee2));

        assertEquals(2, pt.getFollowedusers().size());
        assertTrue(pt.getFollowedusers().contains(trainee1));
        assertTrue(pt.getFollowedusers().contains(trainee2));
    }

    @Test
    void testPersonalTrainerWorkoutPlans() {
        PersonalTrainer pt = new PersonalTrainer(102, "Another PT", 35);
        WorkoutPlan plan1 = new WorkoutPlan(301);
        WorkoutPlan plan2 = new WorkoutPlan(302);

        pt.getWorkoutPlans().add(plan1);
        pt.setWorkoutPlans(List.of(plan1, plan2));

        assertEquals(2, pt.getWorkoutPlans().size());
        assertTrue(pt.getWorkoutPlans().contains(plan1));
        assertTrue(pt.getWorkoutPlans().contains(plan2));
    }

    @Test
    void testUserWorkoutRecord() {
        Trainee trainee = new Trainee(203, "Just Trainee", 22);
        Workout4Record record1 = new Workout4Record("2025-05-27", 401);
        Workout4Record record2 = new Workout4Record("2025-05-28", 402);

        trainee.getWorkoutrecord().add(record1);
        trainee.setWorkoutrecord(List.of(record1, record2));

        assertEquals(2, trainee.getWorkoutrecord().size());
        assertTrue(trainee.getWorkoutrecord().contains(record1));
        assertTrue(trainee.getWorkoutrecord().contains(record2));

        PersonalTrainer pt = new PersonalTrainer(103, "PT with Record", 40);
        pt.getWorkoutrecord().add(record1);
        assertEquals(1, pt.getWorkoutrecord().size());
        assertTrue(pt.getWorkoutrecord().contains(record1));
    }
}
