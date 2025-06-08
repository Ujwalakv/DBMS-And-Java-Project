package ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import ui.OrganizerDashboard;


public class OrganizerDashboard extends JFrame {
    private JLabel welcomeLabel;
    private JTextField eventNameField;
    private JTextField dateField;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JLabel imageLabel;
    private JButton chooseImageButton;
    private JButton addEventButton;
    private File selectedImageFile;
    private String organizerClubName;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public OrganizerDashboard(String clubName) {
        this.organizerClubName = clubName;
        setTitle("Organizer Dashboard");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Profile button
        JButton profileButton = new JButton("Profile");
        profileButton.setFont(new Font("Arial", Font.PLAIN, 14));
        profileButton.addActionListener(e -> showOrganizerProfile());

        welcomeLabel = new JLabel("Hello, welcome back " + clubName + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(profileButton, BorderLayout.WEST);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Event form panel
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        eventNameField = new JTextField();
        dateField = new JTextField("YYYY-MM-DD");
        startTimeField = new JTextField("HH:MM:SS");
        endTimeField = new JTextField("HH:MM:SS");
        imageLabel = new JLabel("No image selected");
        chooseImageButton = new JButton("Choose Image");
        addEventButton = new JButton("Add Event");

        formPanel.add(new JLabel("Event Name:"));
        formPanel.add(eventNameField);
        formPanel.add(new JLabel("Date:"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Start Time:"));
        formPanel.add(startTimeField);
        formPanel.add(new JLabel("End Time:"));
        formPanel.add(endTimeField);
        formPanel.add(imageLabel);
        formPanel.add(chooseImageButton);
        formPanel.add(new JLabel()); // empty cell
        formPanel.add(addEventButton);

        add(formPanel, BorderLayout.CENTER);

        // Image chooser
        chooseImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = fileChooser.getSelectedFile();
                imageLabel.setText(selectedImageFile.getName());
            }
        });

        // Add event button logic
        addEventButton.addActionListener(e -> {
            String ename = eventNameField.getText();
            String edate = dateField.getText();
            String startTime = startTimeField.getText();
            String endTime = endTimeField.getText();

            if (ename.isEmpty() || edate.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || selectedImageFile == null) {
                JOptionPane.showMessageDialog(this, "Please fill all fields and select an image.");
                return;
            }

            // Save event to DB
            try (Connection conn = DriverManager.getConnection(
                    DB_URL, DB_USER, DB_PASSWORD)) {

                // 1. Insert club if not exists
                String checkClubSql = "SELECT clubName FROM clubs WHERE clubName = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkClubSql)) {
                    checkStmt.setString(1, organizerClubName); // Use organizerClubName here
                    ResultSet rs = checkStmt.executeQuery();
                    if (!rs.next()) {
                        // Club does not exist, insert it with default confirmationStatus
                        String insertClubSql = "INSERT INTO clubs (clubName, confirmationStatus) VALUES (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertClubSql)) {
                            insertStmt.setString(1, organizerClubName); // Use organizerClubName here
                            insertStmt.setNull(2, java.sql.Types.VARCHAR); // Set confirmationStatus as NULL
                            insertStmt.executeUpdate();
                        }
                    }
                }

                // 2. Now insert the event as before
                String sql = "INSERT INTO events (ename, date, startTime, endTime, image, clubName) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, ename);
                    stmt.setDate(2, java.sql.Date.valueOf(edate));
                    stmt.setTime(3, java.sql.Time.valueOf(startTime));
                    stmt.setTime(4, java.sql.Time.valueOf(endTime));
                    stmt.setBlob(5, new FileInputStream(selectedImageFile));
                    stmt.setString(6, organizerClubName); // Use organizerClubName here
                    stmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Event added successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding event: " + ex.getMessage());
            }
        });
    }

    private void showOrganizerProfile() {
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBorder(BorderFactory.createTitledBorder("My Events"));

        try (Connection conn = DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD)) {
            // Fetch events hosted by this organizer
            String sql = "SELECT eid, ename, date, startTime, endTime FROM events WHERE clubName = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, organizerClubName); // organizerClubName should be set for this organizer
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        int eid = rs.getInt("eid");
                        String ename = rs.getString("ename");
                        Date date = rs.getDate("date");
                        Time startTime = rs.getTime("startTime");
                        Time endTime = rs.getTime("endTime");

                        JPanel eventCard = new JPanel();
                        eventCard.setLayout(new BoxLayout(eventCard, BoxLayout.Y_AXIS));
                        eventCard.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                        eventCard.setPreferredSize(new Dimension(350, 100));
                        eventCard.add(new JLabel("<html><b>" + ename + "</b><br>Date: " + date + "<br>Time: " + startTime + " - " + endTime + "</html>"));

                        JButton detailsButton = new JButton("Details");
                        detailsButton.addActionListener(e -> showEventRegistrations(eid, ename));
                        eventCard.add(detailsButton);

                        profilePanel.add(eventCard);
                        profilePanel.add(Box.createVerticalStrut(10));
                    }
                    if (!found) {
                        profilePanel.add(new JLabel("No events hosted yet."));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            profilePanel.add(new JLabel("Error loading events: " + ex.getMessage()));
        }

        JScrollPane scrollPane = new JScrollPane(profilePanel);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "Organizer Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showEventRegistrations(int eid, String ename) {
        JDialog dialog = new JDialog(this, "Event Registrations", true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(400, 400));

        java.util.List<Student> students = new java.util.ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT s.sid, s.sname FROM registration_details r JOIN student s ON r.sid = s.sid WHERE r.eid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, eid);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int sid = rs.getInt("sid");
                        String sname = rs.getString("sname");
                        students.add(new Student(sid, sname));
                        panel.add(new JLabel("Student: " + sname + " (ID: " + sid + ")"));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            panel.add(new JLabel("Error loading registrations: " + ex.getMessage()));
        }

        panel.add(Box.createVerticalStrut(20));
        JButton confirmAttendanceButton = new JButton("Confirm Attendance");
        panel.add(confirmAttendanceButton);

        confirmAttendanceButton.addActionListener(e -> {
            confirmAttendanceForEvent(eid, ename, students);
            JOptionPane.showMessageDialog(dialog, "Attendance confirmation sent to relevant teachers (for overlapping classes).");
            dialog.dispose();
        });

        dialog.getContentPane().add(new JScrollPane(panel));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void confirmAttendanceForEvent(int eid, String ename, java.util.List<Student> students) {
    try (Connection conn = DriverManager.getConnection(
            DB_URL, DB_USER, DB_PASSWORD)) {

        for (Student s : students) {
            // Get event details
            String eventSql = "SELECT date, startTime, endTime FROM events WHERE eid = ?";
            Date eventDate = null;
            Time eventStart = null, eventEnd = null;
            try (PreparedStatement eventStmt = conn.prepareStatement(eventSql)) {
                eventStmt.setInt(1, eid);
                try (ResultSet eventRs = eventStmt.executeQuery()) {
                    if (eventRs.next()) {
                        eventDate = eventRs.getDate("date");
                        eventStart = eventRs.getTime("startTime");
                        eventEnd = eventRs.getTime("endTime");
                    }
                }
            }

            if (eventDate == null) continue;

            // Use your SQL to find overlapping classes
            String overlapSql = "SELECT c.tid, ct.cid, ct.day_of_week, ct.start_time, ct.end_time, c.subject " +
                    "FROM attends a " +
                    "JOIN class_timing ct ON a.cid = ct.cid " +
                    "JOIN class c ON c.cid = ct.cid " +
                    "WHERE a.sid = ? " +
                    "AND ct.day_of_week = ? " +
                    "AND ( " +
                    "      (ct.start_time BETWEEN ? AND ?) " +
                    "   OR (ct.end_time BETWEEN ? AND ?) " +
                    "   OR (? BETWEEN ct.start_time AND ct.end_time) " +
                    "   OR (? BETWEEN ct.start_time AND ct.end_time) " +
                    ")";

            String eventDay = new java.text.SimpleDateFormat("EEEE").format(eventDate); // e.g., "Wednesday"
            try (PreparedStatement overlapStmt = conn.prepareStatement(overlapSql)) {
                overlapStmt.setInt(1, s.getSid());
                overlapStmt.setString(2, eventDay);
                overlapStmt.setTime(3, eventStart);
                overlapStmt.setTime(4, eventEnd);
                overlapStmt.setTime(5, eventStart);
                overlapStmt.setTime(6, eventEnd);
                overlapStmt.setTime(7, eventStart);
                overlapStmt.setTime(8, eventEnd);

                try (ResultSet rs = overlapStmt.executeQuery()) {
                    while (rs.next()) {
                        int tid = rs.getInt("tid");
                        int cid = rs.getInt("cid");
                        String subject = rs.getString("subject");
                        // Insert notification for teacher
                        String notifSql = "INSERT INTO teacher_notifications (tid, message, created_at) VALUES (?, ?, NOW())";
                        try (PreparedStatement notifStmt = conn.prepareStatement(notifSql)) {
                            notifStmt.setInt(1, tid);
                            notifStmt.setString(2, "Student " + s.getName() + " missed class (" + subject + ") due to event attendance.");
                            notifStmt.executeUpdate();
                        }
                    }
                }
            }
        }

        // After all notifications are sent, update confirmationStatus
        String updateClubSql = "UPDATE clubs SET confirmationStatus = 'yes' WHERE clubName = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateClubSql)) {
            updateStmt.setString(1, organizerClubName);
            updateStmt.executeUpdate();
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error confirming attendance: " + ex.getMessage());
    }
}

// Helper to check time overlap
private boolean timesOverlap(Time start1, Time end1, Time start2, Time end2) {
    return !start1.after(end2) && !start2.after(end1);
}

class Student {
    private int sid;
    private String name;
    public Student(int sid, String name) {
        this.sid = sid;
        this.name = name;
    }
    public int getSid() { return sid; }
    public String getName() { return name; }
}
}