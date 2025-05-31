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

    public SignupPage() {
        setTitle("Signup Page");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel roleLabel = new JLabel("Role:");
        roleComboBox = new JComboBox<>(new String[]{"Student", "Teacher", "Organizer"});

        JLabel classLabel = new JLabel("Class Name:");
        classNameField = new JTextField();

        signupButton = new JButton("Sign Up");

        // Show/hide classNameField based on role selection
        roleComboBox.addActionListener(e -> {
            String role = ((String) roleComboBox.getSelectedItem()).toLowerCase();
            boolean isStudent = role.equals("student");
            classLabel.setVisible(isStudent);
            classNameField.setVisible(isStudent);
        });
        // Set initial visibility
        classLabel.setVisible(true);
        classNameField.setVisible(true);

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
                            "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
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
                    String email = "";
                    String clubName = "";
                    try (Connection conn = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
                        String insertTeacher = "INSERT INTO teacher (tname, email, clubName, username) VALUES (?, ?, ?, ?)";
                        PreparedStatement stmt = conn.prepareStatement(insertTeacher);
                        stmt.setString(1, tname);
                        stmt.setString(2, email);
                        stmt.setString(3, clubName);
                        stmt.setString(4, username);
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
        add(signupButton);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            SignupPage signupPage = new SignupPage();
            signupPage.setVisible(true);
        });
    }
}