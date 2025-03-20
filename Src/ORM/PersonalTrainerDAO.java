package ORM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonalTrainerDAO {
    private final Connection connection;

    public PersonalTrainerDAO() {
        this.connection = DatabaseManager.getConnection();
    }
    //TODO: vedi se è opportuno inserire anche qui le funzioni che trattano il pt come un traineee, oppure usarle da TraineeDAO ma con l'id del PT

    // CREATE: Add a new Personal Trainer (Insert into AppUser and PersonalTrainersTable)
    public void addPersonalTrainer(String name, int age) {
        String sqlUser = "INSERT INTO AppUser (name, age, is_pt) VALUES (?, ?, true)";
        String sqlTrainer = "INSERT INTO PersonalTrainersTable (user_id) VALUES (currval('AppUser_user_id_seq'))";

        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
            stmtUser.setString(1, name);
            stmtUser.setInt(2, age);
            stmtUser.executeUpdate();
            System.out.println(" Personal Trainer added to AppUser!");

            // Now insert into PersonalTrainersTable
            try (Statement stmtTrainer = connection.createStatement()) {
                stmtTrainer.executeUpdate(sqlTrainer);
                System.out.println(" Personal Trainer added to PersonalTrainersTable!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  READ: Get all users
    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT * FROM AppUser";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add("User ID: " + rs.getInt("user_id") + ", Name: " + rs.getString("name") + ", Age: " + rs.getInt("age") + ", Is PT: " + rs.getBoolean("is_pt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    //  READ: Get all Personal Trainers
    public List<String> getAllPersonalTrainers() {
        List<String> trainers = new ArrayList<>();
        String sql = "SELECT a.user_id, a.name, a.age FROM AppUser a JOIN PersonalTrainersTable pt ON a.user_id = pt.user_id";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trainers.add("Trainer ID: " + rs.getInt("user_id") + ", Name: " + rs.getString("name") + ", Age: " + rs.getInt("age"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trainers;
    }

    //  READ: Get all Workout Plans made by a PT
    public List<String> getPlansMadeByPT(int ptId) {
        List<String> plans = new ArrayList<>();
        String sql = "SELECT wp.wp_id, wp.last_edit_date " +
                "FROM WorkoutPlans wp " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON wp.wp_id = wpt.wp_id " +
                "WHERE wpt.pt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    plans.add("Workout Plan ID: " + rs.getInt("wp_id") + ", Last Edit: " + rs.getString("last_edit_date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    //  READ: Get all users of a PT (users who have a plan created by this PT)
    public List<String> getUsersOfPT(int ptId) {
        List<String> users = new ArrayList<>();
        String sql = "SELECT DISTINCT u.user_id, u.name " +
                "FROM AppUser u " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON u.user_id = wpt.user_id " +
                "WHERE wpt.pt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add("User ID: " + rs.getInt("user_id") + ", Name: " + rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // UPDATE: Edit a Personal Trainer (Modify Name and Age in AppUser)
    public void editPersonalTrainer(int ptId, String newName, int newAge) {
        String sql = "UPDATE AppUser SET name = ?, age = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, newAge);
            stmt.setInt(3, ptId);
            stmt.executeUpdate();
            System.out.println(" Personal Trainer updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  DELETE: Remove a Personal Trainer (Deletes from AppUser → Cascade deletes from PersonalTrainersTable)
    public void deletePersonalTrainer(int ptId) {
        String sql = "DELETE FROM AppUser WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ptId);
            stmt.executeUpdate();
            System.out.println(" Personal Trainer deleted successfully (cascade also removed from PersonalTrainersTable)!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

