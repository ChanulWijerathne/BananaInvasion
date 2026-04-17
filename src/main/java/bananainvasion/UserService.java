package bananainvasion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    // Register new user and send OTP
    public String registerUser(String username, String email, String password) {

        String checkUsername = "SELECT id FROM users WHERE username=?";
        String checkEmail = "SELECT id FROM users WHERE email=?";
        String insert = "INSERT INTO users (username, email, password, otp_code, otp_expiry, verified, high_score) VALUES (?, ?, ?, ?, ?, false, 0)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Check if username already exists
            try (PreparedStatement ps1 = conn.prepareStatement(checkUsername)) {
                ps1.setString(1, username);
                if (ps1.executeQuery().next()) {
                    return "Username already exists.";
                }
            }

            // Check if email already exists
            try (PreparedStatement ps2 = conn.prepareStatement(checkEmail)) {
                ps2.setString(1, email);
                if (ps2.executeQuery().next()) {
                    return "Email already exists.";
                }
            }

            // Generate OTP
            String code = String.valueOf((int) (Math.random() * 900000) + 100000);
            Timestamp expiry = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000));

            // Insert unverified user
            try (PreparedStatement ps3 = conn.prepareStatement(insert)) {
                ps3.setString(1, username);
                ps3.setString(2, email);
                ps3.setString(3, password);
                ps3.setString(4, code);
                ps3.setTimestamp(5, expiry);
                ps3.executeUpdate();
            }

            // Send OTP email
            EmailService.sendOTP(email, code);

            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "Registration failed: " + e.getMessage();
        }
    }

    // Step 1: login username/password, only for verified users
    public boolean loginUser(String username, String password) {

        String sql = "SELECT email FROM users WHERE username=? AND password=? AND verified=true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String email = rs.getString("email");

                // Generate fresh login OTP
                String code = String.valueOf((int) (Math.random() * 900000) + 100000);
                Timestamp expiry = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000));

                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE users SET otp_code=?, otp_expiry=? WHERE username=?"
                )) {
                    update.setString(1, code);
                    update.setTimestamp(2, expiry);
                    update.setString(3, username);
                    update.executeUpdate();
                }

                EmailService.sendOTP(email, code);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Verify OTP
    public boolean verifyOTP(String username, String code) {

        String sql = "SELECT otp_code, otp_expiry FROM users WHERE username=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbCode = rs.getString("otp_code");
                Timestamp expiry = rs.getTimestamp("otp_expiry");

                if (dbCode != null &&
                        dbCode.equals(code) &&
                        expiry != null &&
                        expiry.after(new Timestamp(System.currentTimeMillis()))) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Mark user as verified after successful registration OTP
    public void markUserVerified(String username) {
        String sql = "UPDATE users SET verified=true, otp_code=NULL, otp_expiry=NULL WHERE username=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clear OTP after successful login verification
    public void clearOTP(String username) {
        String sql = "UPDATE users SET otp_code=NULL, otp_expiry=NULL WHERE username=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Delete unverified user if they cancel registration or OTP expires
    public void deleteUnverifiedUser(String username) {
        String sql = "DELETE FROM users WHERE username=? AND verified=false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateHighScore(String username, int score) {
        String sql = "UPDATE users SET high_score = GREATEST(high_score, ?) WHERE username=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, score);
            stmt.setString(2, username);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ScoreEntry> getLeaderboard() {
        List<ScoreEntry> list = new ArrayList<>();

        String sql = "SELECT username, high_score FROM users WHERE verified=true ORDER BY high_score DESC LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new ScoreEntry(
                        rs.getString("username"),
                        rs.getInt("high_score")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}