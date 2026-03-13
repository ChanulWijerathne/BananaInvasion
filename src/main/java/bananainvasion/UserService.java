package bananainvasion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public boolean registerUser(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            return false;
        }
    }

    public void updateHighScore(String username, int score) {
        String sql = "UPDATE users SET high_score = GREATEST(high_score, ?) WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, score);
            stmt.setString(2, username);
            stmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("High score update error: " + e.getMessage());
        }
    }

    public List<ScoreEntry> getLeaderboard() {
        List<ScoreEntry> leaderboard = new ArrayList<>();

        String sql = "SELECT username, high_score FROM users " +
                     "WHERE high_score > 0 " +
                     "ORDER BY high_score DESC, username ASC " +
                     "LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                leaderboard.add(new ScoreEntry(
                        rs.getString("username"),
                        rs.getInt("high_score")
                ));
            }

        } catch (Exception e) {
            System.out.println("Leaderboard error: " + e.getMessage());
        }

        return leaderboard;
    }
}