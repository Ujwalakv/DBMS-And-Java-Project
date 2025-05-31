package models;

public class Teacher extends User {
    private String teacherId;
    private String[] subjectsTaught;

    public Teacher(String username, String password, String role, String teacherId, String[] subjectsTaught) {
        super(username, password, role);
        this.teacherId = teacherId;
        this.subjectsTaught = subjectsTaught;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String[] getSubjectsTaught() {
        return subjectsTaught;
    }

    public void setSubjectsTaught(String[] subjectsTaught) {
        this.subjectsTaught = subjectsTaught;
    }
}