package ORM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TraineeDAO {
    private final Connection connection;

    public TraineeDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    //  CREATE: Add a new Trainee (Insert into AppUser with is_pt = false)
    public void addTrainee(String name, int age) {
        String sql = "INSERT INTO AppUser (name, age, is_pt) VALUES (?, ?, false)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setInt(2, age);
            stmt.executeUpdate();
            System.out.println(" Trainee added successfully!");
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

    //  READ: Get the Workout Plan for a specific User ID
    public List<String> getWorkoutPlanFromUserId(int userId) {
        List<String> workoutPlans = new ArrayList<>();
        String sql = "SELECT wp.wp_id, wp.last_edit_date " +
                "FROM WorkoutPlans wp " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON wp.wp_id = wpt.wp_id " +
                "WHERE wpt.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workoutPlans.add("Workout Plan ID: " + rs.getInt("wp_id") + ", Last Edit: " + rs.getString("last_edit_date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutPlans;
    }

    //  READ: Get the PT assigned to a User ID
    public String getPTForUserId(int userId) {
        String sql = "SELECT pt.user_id, a.name, a.age " +
                "FROM PersonalTrainersTable pt " +
                "JOIN AppUser a ON pt.user_id = a.user_id " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON pt.user_id = wpt.pt_id " +
                "WHERE wpt.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "PT ID: " + rs.getInt("user_id") + ", Name: " + rs.getString("name") + ", Age: " + rs.getInt("age");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No Personal Trainer found for this user.";
    }

    //  UPDATE: Edit a User (Modify Name and Age)
    public void editUser(int userId, String newName, int newAge) {
        String sql = "UPDATE AppUser SET name = ?, age = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, newAge);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
            System.out.println(" User updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  DELETE: Remove a User (Deletes from AppUser → Cascade deletes from related tables)
    public void deleteUser(int userId) {
        String sql = "DELETE FROM AppUser WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println(" User deleted successfully (cascade also removed related records)!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

