package ORM;

import Model.UserManagement.PersonalTrainer;
import Model.UserManagement.Trainee;
import Model.WorkoutManagement.WorkoutPlan;
import Model.WorkoutManagement.WorkoutRecord;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TraineeDAO {
    private final Connection connection;

    public TraineeDAO(Connection connection) {
        this.connection = connection;
    }

    public TraineeDAO() {
        this(DatabaseManager.getConnection());
    }

    public Trainee addTrainee(String name, int age) {
        String sqlUser = "INSERT INTO AppUser (user_name, user_age, is_pt) VALUES (?, ?, false)";

        try (PreparedStatement stmt = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setInt(2, age);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted == 0) {
                System.out.println("No Trainee was added.");
                return null;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    System.out.println("Trainee added successfully with ID: " + userId);
                    return new Trainee(userId, name, age);
                } else {
                    System.out.println("Failed to get generated user ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Insertion failed
    }

    // READ: Get all users (returning mixed Trainee & PT objects)
    public List<Trainee> getAllTrainees() {
        List<Trainee> trainees = new ArrayList<>();
        String sql = "SELECT * FROM AppUser WHERE is_pt = false";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trainees.add(new Trainee(
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getInt("user_age")
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
                "WHERE wpt.trainee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workoutPlans.add(new WorkoutPlan( rs.getInt("wp_id")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workoutPlans;
    }

    //  READ: Get the PT assigned to a User ID (returning a PersonalTrainer object)
    public PersonalTrainer getPTForUserId(int userId) {
        String sql = "SELECT pt.pt_id, a.user_name, a.user_age " +
                "FROM Personal_Trainer pt " +
                "JOIN AppUser a ON pt.pt_id = a.user_id " +
                "JOIN WorkoutPlans_PersonalTrainer_AppUser wpt ON pt.pt_id = wpt.pt_id " +
                "WHERE wpt.trainee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PersonalTrainer(
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getInt("user_age")
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
        String sql = "UPDATE AppUser SET user_name = ?, user_age = ? WHERE user_id = ?";
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

    public WorkoutRecord getWorkoutRecordByUserId(int userId) {
        String sql = "SELECT wr.wr_id, wr.last_edit_date " +
                "FROM WorkoutRecords wr " +
                "JOIN WorkoutRecords_AppUser wra ON wr.wr_id = wra.wr_id " +
                "WHERE wra.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("wr_id");
                    Date creationDate = rs.getDate("last_edit_date");
                    WorkoutRecord record = new WorkoutRecord(id);
                    // You might want to fetch more details of the WorkoutRecord if needed
                    return record;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}


