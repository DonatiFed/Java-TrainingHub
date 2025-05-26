package ORM;

import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.UserManagement.User;
import Model.WorkoutManagement.WorkoutPlan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonalTrainerDAO {
    private final Connection connection;

    public PersonalTrainerDAO(Connection connection) {
        this.connection = connection;
    }

    public PersonalTrainerDAO() {
        this(DatabaseManager.getConnection());
    }

    // CREATE: Add a new Personal Trainer (AppUser + Personal_Trainer)
    public PersonalTrainer addPersonalTrainer(String name, int age) {
        String sqlUser = "INSERT INTO AppUser (user_name, user_age, is_pt) VALUES (?, ?, true)";
        String sqlTrainer = "INSERT INTO Personal_Trainer (pt_id) VALUES (?)";

        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
            stmtUser.setString(1, name);
            stmtUser.setInt(2, age);

            int rowsInserted = stmtUser.executeUpdate();

            if (rowsInserted == 0) {
                System.out.println("No PersonalTrainer was added.");
                return null;
            }

            try (ResultSet generatedKeys = stmtUser.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int ptId = generatedKeys.getInt(1);

                    try (PreparedStatement stmtTrainer = connection.prepareStatement(sqlTrainer)) {
                        stmtTrainer.setInt(1, ptId);
                        stmtTrainer.executeUpdate();
                    }

                    return new PersonalTrainer(ptId, name, age);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public List<PersonalTrainer> getAllPersonalTrainers() {
        List<PersonalTrainer> trainers = new ArrayList<>();
        String sql = "SELECT a.user_id, a.user_name, a.user_age FROM AppUser a JOIN Personal_Trainer pt ON a.user_id = pt.pt_id";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("user_name");
                int age = rs.getInt("user_age");

                PersonalTrainer trainer = new PersonalTrainer(id, name, age);
                trainer.setFollowedusers(getTraineesOfPT(id));
                trainer.setWorkoutPlans(getPlansMadeByPT(id));
                trainers.add(trainer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trainers;
    }

    public PersonalTrainer getPersonalTrainerById(int ptId) {
        String sql = "SELECT a.user_id, a.user_name, a.user_age FROM AppUser a JOIN Personal_Trainer pt ON a.user_id = pt.pt_id WHERE a.user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("user_name");
                    int age = rs.getInt("user_age");

                    PersonalTrainer trainer = new PersonalTrainer(ptId, name, age);
                    trainer.setFollowedusers(getTraineesOfPT(ptId));
                    trainer.setWorkoutPlans(getPlansMadeByPT(ptId));

                    return trainer;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<WorkoutPlan> getPlansMadeByPT(int ptId) {
        List<WorkoutPlan> plans = new ArrayList<>();
        String sql = "SELECT wp.wp_id, wp.last_edit_date FROM WorkoutPlans wp JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON wp.wp_id = wpt.wp_id WHERE wpt.pt_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("wp_id");
                    String lastEditDate = rs.getString("last_edit_date");

                    WorkoutPlan workoutPlan = new WorkoutPlan(id);
                    workoutPlan.setLastEditDate(lastEditDate);
                    plans.add(workoutPlan);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    public List<User> getTraineesOfPT(int ptId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.user_id, u.user_name, u.user_age, u.is_pt FROM AppUser u JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON u.user_id = wpt.trainee_id WHERE wpt.pt_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("user_id");
                    String name = rs.getString("user_name");
                    int age = rs.getInt("user_age");
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

    public void editPersonalTrainer(int ptId, String newName, int newAge) {
        String sql = "UPDATE AppUser SET user_name = ?, user_age = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, newAge);
            stmt.setInt(3, ptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePersonalTrainer(int ptId) {
        String sql = "DELETE FROM AppUser WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isFollowing(int ptId, int traineeId) {
        String sql = "SELECT 1 FROM WorkoutPlans_PersonalTrainer_AppUser WHERE pt_id = ? AND trainee_id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            stmt.setInt(2, traineeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // returns true if a record exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
