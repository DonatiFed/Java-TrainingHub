package ORM;

import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.WorkoutManagement.WorkoutPlan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TraineeDAO {
    private final Connection connection;

    public TraineeDAO() {
        this.connection = DatabaseManager.getConnection();
    }

    //  CREATE: Add a new Trainee
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

    // READ: Get all users (returning mixed Trainee & PT objects)
    public List<Trainee> getAllTrainees() {
        List<Trainee> trainees = new ArrayList<>();
        String sql = "SELECT * FROM AppUser WHERE is_pt = false";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trainees.add(new Trainee(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getInt("age")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trainees;
    }

    //  READ: Get the WorkoutPlan for a specific User ID
    public List<WorkoutPlan> getWorkoutPlanFromUserId(int userId) {
        List<WorkoutPlan> workoutPlans = new ArrayList<>();
        String sql = "SELECT wp.wp_id, wp.last_edit_date " +
                "FROM WorkoutPlans wp " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON wp.wp_id = wpt.wp_id " +
                "WHERE wpt.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workoutPlans.add(new WorkoutPlan(null, rs.getInt("wp_id")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutPlans;
    }

    //  READ: Get the PT assigned to a User ID (returning a PersonalTrainer object)
    public PersonalTrainer getPTForUserId(int userId) {
        String sql = "SELECT pt.user_id, a.name, a.age " +
                "FROM PersonalTrainersTable pt " +
                "JOIN AppUser a ON pt.user_id = a.user_id " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON pt.user_id = wpt.pt_id " +
                "WHERE wpt.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PersonalTrainer(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getInt("age")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No PT found
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

    //  DELETE: Remove a User (Cascade deletes from related tables)
    public void deleteUser(int userId) {
        String sql = "DELETE FROM AppUser WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println(" User deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


