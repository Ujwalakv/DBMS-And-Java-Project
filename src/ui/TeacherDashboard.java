package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.border.EmptyBorder;

public class TeacherDashboard extends JFrame {
    private int tid;
    private String tname;
    private JPanel classesPanel;

    public TeacherDashboard(int tid, String tname) {
        this.tid = tid;
        this.tname = tname;

        setTitle("Teacher Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Hello, welcome back " + tname + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton profileButton = new JButton("View Profile");
        profileButton.setFont(new Font("Arial", Font.PLAIN, 14));
        profileButton.addActionListener(e -> showProfileDialog());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(profileButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        classesPanel = new JPanel();
        classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(classesPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Add the "Add Class" card FIRST
        addClassCard(); // <-- Make sure this is called before loading classes

        loadClassesAndStudents();
    }

    private void loadClassesAndStudents() {
        classesPanel.removeAll();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
            // Get all classes handled by this teacher
            String classSql = "SELECT cid, sem, dept, subject FROM class WHERE tid = ?";
            try (PreparedStatement classStmt = conn.prepareStatement(classSql)) {
                classStmt.setInt(1, tid);
                try (ResultSet classRs = classStmt.executeQuery()) {
                    while (classRs.next()) {
                        int cid = classRs.getInt("cid");
                        int sem = classRs.getInt("sem");
                        String dept = classRs.getString("dept");
                        String subject = classRs.getString("subject");

                        // Card panel for each class
                        JPanel card = new JPanel();
                        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                        card.setBorder(BorderFactory.createTitledBorder("Class: " + subject + " | " + dept + " Sem " + sem + " (ID: " + cid + ")"));
                        card.setBackground(new Color(240, 248, 255));
                        card.setMaximumSize(new Dimension(800, 200));

                        // Get students in this class (using attends table)
                        String studentSql = "SELECT s.sid, s.sname FROM attends a JOIN student s ON a.sid = s.sid WHERE a.cid = ? ORDER BY s.sname ASC";
                        try (PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {
                            studentStmt.setInt(1, cid);
                            try (ResultSet studentRs = studentStmt.executeQuery()) {
                                int studentCount = 0;
                                while (studentRs.next()) {
                                    studentCount++;
                                    int sid = studentRs.getInt("sid");
                                    String sname = studentRs.getString("sname");

                                    JLabel studentLabel = new JLabel("Student: " + sname + " (ID: " + sid + ")");
                                    card.add(studentLabel);
                                }
                                JLabel countLabel = new JLabel("Total Students: " + studentCount);
                                countLabel.setFont(new Font("Arial", Font.BOLD, 12));
                                card.add(countLabel);
                            }
                        }
                        classesPanel.add(card);
                        classesPanel.add(Box.createVerticalStrut(20));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading classes: " + ex.getMessage());
        }
        // Add the "Add Class" card at the end
        addClassCard();
        classesPanel.revalidate();
        classesPanel.repaint();
    }

    private void addClassCard() {
        JPanel addClassCard = new JPanel();
        addClassCard.setLayout(new BoxLayout(addClassCard, BoxLayout.Y_AXIS));
        addClassCard.setBorder(BorderFactory.createTitledBorder("Add Class"));
        addClassCard.setBackground(new Color(230, 255, 230));
        addClassCard.setMaximumSize(new Dimension(800, 100));
        addClassCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton plusButton = new JButton("+");
        plusButton.setFont(new Font("Arial", Font.BOLD, 32));
        plusButton.setPreferredSize(new Dimension(60, 60));
        plusButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        plusButton.addActionListener(e -> showAddClassForm());

        JLabel addLabel = new JLabel("Add Class Details");
        addLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        addLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        addClassCard.add(Box.createVerticalStrut(10));
        addClassCard.add(plusButton);
        addClassCard.add(addLabel);
        addClassCard.add(Box.createVerticalStrut(10));

        classesPanel.add(addClassCard);
        classesPanel.add(Box.createVerticalStrut(20));
    }

    private void showAddClassForm() {
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField subjectField = new JTextField();
        JTextField semField = new JTextField();
        JTextField deptField = new JTextField();
        JTextField classNameField = new JTextField();

        form.add(new JLabel("Subject:"));
        form.add(subjectField);
        form.add(new JLabel("Semester:"));
        form.add(semField);
        form.add(new JLabel("Department:"));
        form.add(deptField);
        form.add(new JLabel("Class Name:"));
        form.add(classNameField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Class", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String subject = subjectField.getText();
                int sem = Integer.parseInt(semField.getText());
                String dept = deptField.getText();
                String className = classNameField.getText().trim();

                int cid = -1;
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
                    // Insert into class table
                    String sql = "INSERT INTO class (subject, sem, dept, tid) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, subject);
                        stmt.setInt(2, sem);
                        stmt.setString(3, dept);
                        stmt.setInt(4, tid);
                        stmt.executeUpdate();
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            cid = rs.getInt(1);
                        }
                    }
                    // Automatically add all students of that sem & dept to attends
                    if (cid != -1) {
                        String studentSql = "SELECT sid FROM student WHERE class_name = ?";
                        try (PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {
                            studentStmt.setString(1, className);
                            try (ResultSet studentRs = studentStmt.executeQuery()) {
                                String attendsSql = "INSERT INTO attends (cid, sid) VALUES (?, ?)";
                                try (PreparedStatement attendsStmt = conn.prepareStatement(attendsSql)) {
                                    while (studentRs.next()) {
                                        int sid = studentRs.getInt("sid");
                                        attendsStmt.setInt(1, cid);
                                        attendsStmt.setInt(2, sid);
                                        attendsStmt.addBatch();
                                    }
                                    attendsStmt.executeBatch();
                                }
                            }
                        }
                        showAddClassTimingsForm(cid);
                        JOptionPane.showMessageDialog(this, "Class and students added successfully!");
                        loadClassesAndStudents();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding class: " + ex.getMessage());
            }
        }
    }

    private void showAddStudentsToClassForm(int cid) {
        String sidInput = JOptionPane.showInputDialog(this, "Enter Student IDs (comma separated):");
        if (sidInput != null && !sidInput.trim().isEmpty()) {
            String[] sidStrings = sidInput.split(",");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
                String sql = "INSERT INTO attends (cid, sid) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (String sidStr : sidStrings) {
                        int sid = Integer.parseInt(sidStr.trim());
                        stmt.setInt(1, cid);
                        stmt.setInt(2, sid);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding students: " + ex.getMessage());
            }
        }
    }

    private void showAddClassTimingsForm(int cid) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        JPanel timingPanel = new JPanel(new GridLayout(days.length + 1, 3, 10, 5));
        timingPanel.add(new JLabel("Day"));
        timingPanel.add(new JLabel("Start Time (HH:MM)"));
        timingPanel.add(new JLabel("End Time (HH:MM)"));

        JTextField[] startFields = new JTextField[days.length];
        JTextField[] endFields = new JTextField[days.length];

        for (int i = 0; i < days.length; i++) {
            timingPanel.add(new JLabel(days[i]));
            startFields[i] = new JTextField();
            endFields[i] = new JTextField();
            timingPanel.add(startFields[i]);
            timingPanel.add(endFields[i]);
        }

        int result = JOptionPane.showConfirmDialog(this, timingPanel, "Add Class Timings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
                String sql = "INSERT INTO class_timing (cid, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < days.length; i++) {
                        String start = startFields[i].getText().trim();
                        String end = endFields[i].getText().trim();
                        if (!start.isEmpty() && !end.isEmpty()) {
                            stmt.setInt(1, cid);
                            stmt.setString(2, days[i]);
                            stmt.setTime(3, java.sql.Time.valueOf(start + ":00"));
                            stmt.setTime(4, java.sql.Time.valueOf(end + ":00"));
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding timings: " + ex.getMessage());
            }
        }
    }

    private void showProfileDialog() {
        StringBuilder profile = new StringBuilder();
        profile.append("Teacher ID: ").append(tid).append("\n");
        profile.append("Name: ").append(tname).append("\n");

        // Fetch email and clubName
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")) {
            String teacherSql = "SELECT email, clubName FROM teacher WHERE tid = ?";
            try (PreparedStatement teacherStmt = conn.prepareStatement(teacherSql)) {
                teacherStmt.setInt(1, tid);
                try (ResultSet rs = teacherStmt.executeQuery()) {
                    if (rs.next()) {
                        profile.append("Email: ").append(rs.getString("email")).append("\n");
                        profile.append("Club Name: ").append(rs.getString("clubName")).append("\n");
                    }
                }
            }

            // Fetch classes and students
            String classSql = "SELECT cid, sem, dept FROM class WHERE tid = ?";
            try (PreparedStatement classStmt = conn.prepareStatement(classSql)) {
                classStmt.setInt(1, tid);
                try (ResultSet classRs = classStmt.executeQuery()) {
                    while (classRs.next()) {
                        int cid = classRs.getInt("cid");
                        int sem = classRs.getInt("sem");
                        String dept = classRs.getString("dept");
                        profile.append("\nClass: ").append(dept).append(" Sem ").append(sem).append(" (ID: ").append(cid).append(")\n");

                        // Fetch students in this class
                        String studentSql = "SELECT s.sid, s.sname FROM attends a JOIN student s ON a.sid = s.sid WHERE a.cid = ? ORDER BY s.sname ASC";
                        try (PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {
                            studentStmt.setInt(1, cid);
                            try (ResultSet studentRs = studentStmt.executeQuery()) {
                                while (studentRs.next()) {
                                    int sid = studentRs.getInt("sid");
                                    String sname = studentRs.getString("sname");
                                    profile.append("    Student: ").append(sname).append(" (ID: ").append(sid).append(")\n");
                                }
                            }
                        }
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
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "Teacher Profile", JOptionPane.INFORMATION_MESSAGE);
    }
}