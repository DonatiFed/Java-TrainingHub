package ORM;

import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.UserManagement.User;
import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.WorkoutRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonalTrainerDAO {
    private final Connection connection;

    public PersonalTrainerDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    //  CREATE: Add a new Personal Trainer (Insert into AppUser and PersonalTrainersTable)
    public PersonalTrainer addPersonalTrainer(String name, int age) {
        String sqlUser = "INSERT INTO AppUser (user_name, user_age, is_pt) VALUES (?, ?, true) RETURNING user_id";
        String sqlTrainer = "INSERT INTO Personal_Trainer (pt_id) VALUES (?)";
        String sqlLinkWorkoutRecord = "INSERT INTO WorkoutRecords_AppUser (user_id, wr_id) VALUES (?, ?)";

        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser)) {
            stmtUser.setString(1, name);
            stmtUser.setInt(2, age);
            ResultSet rs = stmtUser.executeQuery();

            if (rs.next()) {
                int ptId = rs.getInt("user_id");

                // Insert into PersonalTrainersTable
                try (PreparedStatement stmtTrainer = connection.prepareStatement(sqlTrainer)) {
                    stmtTrainer.setInt(1, ptId);
                    stmtTrainer.executeUpdate();
                }

                // Create WorkoutRecord for Personal Trainer
                WorkoutRecordDAO workoutRecordDAO = new WorkoutRecordDAO();
                WorkoutRecord workoutRecord = workoutRecordDAO.addWorkoutRecord();

                if (workoutRecord != null) {
                    // Link WorkoutRecord to Personal Trainer
                    try (PreparedStatement stmtLink = connection.prepareStatement(sqlLinkWorkoutRecord)) {
                        stmtLink.setInt(1, ptId);
                        stmtLink.setInt(2, workoutRecord.getId());
                        stmtLink.executeUpdate();
                    }
                }

                return new PersonalTrainer(ptId, name, age);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    //  READ: Get all Personal Trainers
    public List<PersonalTrainer> getAllPersonalTrainers() {
        List<PersonalTrainer> trainers = new ArrayList<>();
        String sql = "SELECT a.user_id, a.user_name, a.user_age FROM AppUser a JOIN Personal_Trainer pt ON a.user_id = pt.pt_id";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("user_name");
                int age = rs.getInt("user_age");

                PersonalTrainer trainer = new PersonalTrainer(id, name, age);
                trainer.setFollowedusers(getUsersOfPT(id));  // Attach trainees
                trainer.setWorkoutPlans(getPlansMadeByPT(id)); // Attach workout plans

                trainers.add(trainer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trainers;
    }

    // READ: Get a single Personal Trainer by ID
    public PersonalTrainer getPersonalTrainerById(int ptId) {
        String sql = "SELECT a.user_id, a.user_name, a.user_age FROM AppUser a JOIN Personal_Trainer pt ON a.user_id = pt.pt_id WHERE a.user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    int age = rs.getInt("age");

                    PersonalTrainer trainer = new PersonalTrainer(ptId, name, age);
                    trainer.setFollowedusers(getUsersOfPT(ptId));
                    trainer.setWorkoutPlans(getPlansMadeByPT(ptId));

                    return trainer;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //  READ: Get all Workout Plans made by a PT (returns WorkoutPlan objects)
    public List<WorkoutPlan> getPlansMadeByPT(int ptId) {
        List<WorkoutPlan> plans = new ArrayList<>();
        String sql = "SELECT wp.wp_id, wp.last_edit_date FROM WorkoutPlans wp JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON wp.wp_id = wpt.wp_id WHERE wpt.pt_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("wp_id");
                    String lastEditDate = rs.getString("last_edit_date");

                    WorkoutPlan workoutPlan = new WorkoutPlan( id);
                    workoutPlan.setLastEditDate(lastEditDate);
                    plans.add(workoutPlan);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    //  READ: Get all users trained by a PT (returns a mix of Trainees & PersonalTrainers)
    public List<User> getUsersOfPT(int ptId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.user_id, u.user_name, u.user_age, u.is_pt " +
                "FROM AppUser u " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt " +
                "ON u.user_id = wpt.trainee_id " +
                "WHERE wpt.pt_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("user_id");
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    boolean isPT = rs.getBoolean("is_pt");

                    if (isPT) {
                        users.add(new PersonalTrainer(id, name, age));
                    } else {
                        users.add(new Trainee(id, name, age));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }


    //  UPDATE: Modify Personal Trainer's name and age
    public void editPersonalTrainer(int ptId, String newName, int newAge) {
        String sql = "UPDATE AppUser SET user_name = ?, user_age = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, newAge);
            stmt.setInt(3, ptId);
            stmt.executeUpdate();
            System.out.println("Personal Trainer updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  DELETE: Remove a Personal Trainer (cascade deletes from PersonalTrainersTable)
    public void deletePersonalTrainer(int ptId) {
        String sql = "DELETE FROM AppUser WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            stmt.executeUpdate();
            System.out.println("Personal Trainer deleted successfully (cascade also removed from PersonalTrainersTable)!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


