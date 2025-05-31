package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private JLabel messageLabel;

    public LoginPage() {
        setTitle("Login Page");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        add(loginButton);

        signupButton = new JButton("Sign Up");
        add(signupButton);

        JButton forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setForeground(Color.BLUE);
        add(forgotPasswordButton);

        messageLabel = new JLabel("");
        add(messageLabel);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            services.AuthService authService = new services.AuthService();
            String role = authService.loginAndGetRole(username, password);

            if (role == null) {
                messageLabel.setText("Invalid username or password.");
            } else {
                switch (role) {
                    case "organizer":
                        new OrganizerDashboard(username).setVisible(true);
                        break;
                    case "teacher":
                        try {
                            // Connect to DB and get tid and tname using username
                            Connection conn = DriverManager.getConnection(
                                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password");
                            String sql = "SELECT tid, tname FROM teacher WHERE username = ?";
                            PreparedStatement stmt = conn.prepareStatement(sql);
                            stmt.setString(1, username);
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()) {
                                int tid = rs.getInt("tid");
                                String tname = rs.getString("tname");
                                new TeacherDashboard(tid, tname).setVisible(true);
                            } else {
                                JOptionPane.showMessageDialog(null, "Teacher not found.");
                            }
                            conn.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                        }
                        break;
                    case "student":
                        try {
                            // Connect to DB and get sid using sname (username)
                            Connection conn = DriverManager.getConnection(
                                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password");
                            String sql = "SELECT sid, sname FROM student WHERE username = ?";
                            PreparedStatement stmt = conn.prepareStatement(sql);
                            stmt.setString(1, username);
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()) {
                                int sid = rs.getInt("sid");
                                String sname = rs.getString("sname");
                                new StudentDashboard(sid, sname).setVisible(true);
                            } else {
                                JOptionPane.showMessageDialog(null, "Student not found.");
                            }
                            conn.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                        }
                        break;
                }
                dispose(); // Close the login page after successful login
            }
        });

        signupButton.addActionListener(e -> {
            new SignupPage().setVisible(true);
            // Optionally dispose(); if you want to close login page
        });

        forgotPasswordButton.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Enter your username:");
            if (username != null && !username.trim().isEmpty()) {
                String newPassword = JOptionPane.showInputDialog(this, "Enter your new password:");
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    try (Connection conn = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/attendance_system", "user", "password")) {
                        String sql = "UPDATE users SET password = ? WHERE username = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, newPassword);
                            stmt.setString(2, username);
                            int updated = stmt.executeUpdate();
                            if (updated > 0) {
                                JOptionPane.showMessageDialog(this, "Password updated successfully!");
                            } else {
                                JOptionPane.showMessageDialog(this, "Username not found.");
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);
        });
    }
}
