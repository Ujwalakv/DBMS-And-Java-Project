package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;

public class StudentDashboard extends JFrame {
    private int sid;
    private String sname;
    private JPanel eventsPanel;
    private JPanel teachersPanel;

    public StudentDashboard(int sid, String sname) {
        this.sid = sid;
        this.sname = sname;

        setTitle("Student Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Hello, welcome back " + sname + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton profileButton = new JButton("Profile");
        profileButton.setFont(new Font("Arial", Font.PLAIN, 14));
        profileButton.addActionListener(e -> showStudentProfile());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(profileButton, BorderLayout.WEST);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        eventsPanel = new JPanel();
        eventsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        JScrollPane scrollPane = new JScrollPane(eventsPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadEvents();
        loadMyTeachers();
    }

    private void loadEvents() {
        eventsPanel.removeAll();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
            String sql = "SELECT eid, ename, date, startTime, endTime, image FROM events";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int eid = rs.getInt("eid");
                    String ename = rs.getString("ename");
                    Date date = rs.getDate("date");
                    Time startTime = rs.getTime("startTime");
                    Time endTime = rs.getTime("endTime");
                    byte[] imageBytes = rs.getBytes("image");

                    JPanel card = new JPanel();
                    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                    card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    card.setPreferredSize(new Dimension(200, 300));

                    // Image
                    JLabel imageLabel;
                    if (imageBytes != null) {
                        ImageIcon icon = new ImageIcon(imageBytes);
                        Image img = icon.getImage().getScaledInstance(180, 120, Image.SCALE_SMOOTH);
                        imageLabel = new JLabel(new ImageIcon(img));
                    } else {
                        imageLabel = new JLabel("No Image");
                        imageLabel.setPreferredSize(new Dimension(180, 120));
                    }
                    imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    card.add(imageLabel);

                    // Title
                    JLabel titleLabel = new JLabel(ename, SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    card.add(titleLabel);

                    // Details
                    JLabel dateLabel = new JLabel("Date: " + date);
                    JLabel timeLabel = new JLabel("Time: " + startTime + " - " + endTime);
                    dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    card.add(dateLabel);
                    card.add(timeLabel);

                    // Register Button
                    JButton registerButton = new JButton("Register");
                    registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                    registerButton.addActionListener(e -> showEventRegistrationForm(eid, ename));
                    card.add(Box.createVerticalStrut(10));
                    card.add(registerButton);

                    eventsPanel.add(card);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading events: " + ex.getMessage());
        }
        eventsPanel.revalidate();
        eventsPanel.repaint();
    }

    private void loadMyTeachers() {
        if (teachersPanel != null) {
            getContentPane().remove(teachersPanel);
        }
        teachersPanel = new JPanel();
        teachersPanel.setLayout(new BoxLayout(teachersPanel, BoxLayout.Y_AXIS));
        teachersPanel.setBorder(BorderFactory.createTitledBorder("My Teachers"));

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
            String sql = "SELECT t.tname, t.email, c.dept, c.sem, c.subject " +
                         "FROM attends a " +
                         "JOIN class c ON a.cid = c.cid " +
                         "JOIN teacher t ON c.tid = t.tid " +
                         "WHERE a.sid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, sid);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String tname = rs.getString("tname");
                        String email = rs.getString("email");
                        String dept = rs.getString("dept");
                        int sem = rs.getInt("sem");
                        String subject = rs.getString("subject");
                        JLabel label = new JLabel(
                            "<html><b>" + tname + "</b> (" + email + ")<br>Subject: " + subject + "<br>Department: " + dept + ", Semester: " + sem + "</html>"
                        );
                        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                        teachersPanel.add(label);
                    }
                    if (!found) {
                        teachersPanel.add(new JLabel("No teachers assigned yet."));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            teachersPanel.add(new JLabel("Error loading teachers: " + ex.getMessage()));
        }

        getContentPane().add(teachersPanel, BorderLayout.SOUTH);
        teachersPanel.revalidate();
        teachersPanel.repaint();
    }

    private void registerForEvent(int eid, String ename) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
            // Check if already registered
            String checkSql = "SELECT * FROM participatesIn WHERE sid = ? AND eid = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, sid);
                checkStmt.setInt(2, eid);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "You are already registered for this event.");
                    return;
                }
            }
            // Register
            String sql = "INSERT INTO participatesIn (sid, eid) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, sid);
                stmt.setInt(2, eid);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registered for event: " + ename);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering: " + ex.getMessage());
        }
    }

    private void insertStudent(String username) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
            String insertStudent = "INSERT INTO student (sname, username) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertStudent);
            stmt.setString(1, sname); // student's name from signup form
            stmt.setString(2, username); // username from signup form
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inserting student: " + ex.getMessage());
        }
    }

    private void showEventRegistrationForm(int eid, String ename) {
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField usnField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField deptField = new JTextField();
        JTextField semField = new JTextField();

        form.add(new JLabel("USN:"));
        form.add(usnField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Department:"));
        form.add(deptField);
        form.add(new JLabel("Semester:"));
        form.add(semField);

        int result = JOptionPane.showConfirmDialog(this, form, "Register for " + ename, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String usn = usnField.getText();
            String name = nameField.getText();
            String dept = deptField.getText();
            String sem = semField.getText();

            // Insert into participatesIn and optionally a registration details table
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
                // Insert into participatesIn
                String sql = "INSERT INTO participatesIn (sid, eid) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, sid);
                    stmt.setInt(2, eid);
                    stmt.executeUpdate();
                }
                // Optionally, create and insert into a registration_details table for more info
                String regSql = "INSERT INTO registration_details (sid, eid, usn, name, dept, sem) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement regStmt = conn.prepareStatement(regSql)) {
                    regStmt.setInt(1, sid);
                    regStmt.setInt(2, eid);
                    regStmt.setString(3, usn);
                    regStmt.setString(4, name);
                    regStmt.setString(5, dept);
                    regStmt.setString(6, sem);
                    regStmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Registered for event: " + ename);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error registering: " + ex.getMessage());
            }
        }
    }

    private void showStudentProfile() {
        StringBuilder profile = new StringBuilder();
        profile.append("Student ID: ").append(sid).append("\n");
        profile.append("Name: ").append(sname).append("\n");

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
            // Fetch registration details
            String sql = "SELECT e.ename FROM participatesIn p JOIN events e ON p.eid = e.eid WHERE p.sid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, sid);
                try (ResultSet rs = stmt.executeQuery()) {
                    profile.append("\nRegistered Events:\n");
                    while (rs.next()) {
                        profile.append("  - ").append(rs.getString("ename")).append("\n");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
            return;
        }

        JTextArea textArea = new JTextArea(profile.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "Student Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void login(String username, String password) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Login success
                int userType = rs.getInt("user_type");
                if (userType == 1) {
                    // Admin
                    // new AdminDashboard().setVisible(true); // <-- Remove or comment this line
                } else {
                    // Student
                    int sid = rs.getInt("id");
                    String sname = rs.getString("sname");
                    new StudentDashboard(sid, sname).setVisible(true);
                }
                dispose();
            } else {
                // Login failed
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during login: " + ex.getMessage());
        }
    }
}
