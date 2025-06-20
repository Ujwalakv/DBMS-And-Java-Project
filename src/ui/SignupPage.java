package ui;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SignupPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton signupButton;
    private JTextField classNameField; // Only this is needed now
    private JTextField emailField;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public SignupPage() {
        setTitle("Signup Page");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel roleLabel = new JLabel("Role:");
        roleComboBox = new JComboBox<>(new String[]{"Student", "Teacher", "Organizer"});

        JLabel classLabel = new JLabel("Class Name:");
        classNameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        signupButton = new JButton("Sign Up");

        // Show/hide classNameField and emailField based on role selection
        roleComboBox.addActionListener(e -> {
            String role = ((String) roleComboBox.getSelectedItem()).toLowerCase();
            boolean isStudent = role.equals("student");
            boolean isTeacher = role.equals("teacher");
            classLabel.setVisible(isStudent);
            classNameField.setVisible(isStudent);
            emailLabel.setVisible(isTeacher);
            emailField.setVisible(isTeacher);
        });
        // Set initial visibility
        classLabel.setVisible(true);
        classNameField.setVisible(true);
        emailLabel.setVisible(false);
        emailField.setVisible(false);

        signupButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = ((String) roleComboBox.getSelectedItem()).toLowerCase();

            services.AuthService authService = new services.AuthService();
            boolean success = authService.signup(username, password, role);

            if (success) {
                if (role.equals("student")) {
                    String sname = username; // or ask for real name
                    String className = classNameField.getText().trim();
                    try (Connection conn = DriverManager.getConnection(
                            DB_URL, DB_USER, DB_PASSWORD)) {
                        String insertStudent = "INSERT INTO student (sname, username, class_name) VALUES (?, ?, ?)";
                        PreparedStatement stmt = conn.prepareStatement(insertStudent);
                        stmt.setString(1, sname);
                        stmt.setString(2, username);
                        stmt.setString(3, className);
                        stmt.executeUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error inserting student: " + ex.getMessage());
                    }
                } else if (role.equals("teacher")) {
                    String tname = username;
                    String email = emailField.getText().trim();
                    try (Connection conn = DriverManager.getConnection(
                            DB_URL, DB_USER, DB_PASSWORD)) {
                        String insertTeacher = "INSERT INTO teacher (tname, email, username) VALUES (?, ?, ?)";
                        PreparedStatement stmt = conn.prepareStatement(insertTeacher);
                        stmt.setString(1, tname);
                        stmt.setString(2, email);
                        stmt.setString(3, username);
                        stmt.executeUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error inserting teacher: " + ex.getMessage());
                    }
                } else if (role.equals("organizer")) {
                    // Organizer signup logic here (if needed)
                }
                JOptionPane.showMessageDialog(this, "Signup successful!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Signup failed! Username may already exist.");
            }
        });

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(roleLabel);
        add(roleComboBox);
        add(classLabel);
        add(classNameField);
        add(emailLabel);
        add(emailField);
        add(signupButton);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            SignupPage signupPage = new SignupPage();
            signupPage.setVisible(true);
        });
    }
}