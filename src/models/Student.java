package models;
public class Student extends User {
    private String studentId;
    private String[] enrolledCourses;

    public Student(String username, String password, String role, String studentId, String[] enrolledCourses) {
        super(username, password, role);
        this.studentId = studentId;
        this.enrolledCourses = enrolledCourses;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String[] getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(String[] enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }
}