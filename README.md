# Role-Based Login App

## Overview

This is a Java Swing application for managing attendance and events in a college environment.  
It supports three roles: **Student**, **Teacher**, and **Organizer**.

---

## Features

- **Role-based login and signup** (Student, Teacher, Organizer)
- **Student**
  - Register for events
  - View class details and teachers
  - Attendance is automatically granted for classes missed due to event participation (if confirmed by organizer)
- **Teacher**
  - Add classes (with subject, semester, department, class name)
  - Add class timings for each day of the week
  - View students in each class (alphabetically)
  - Receive notifications when students attend events during class time
- **Organizer**
  - Create and manage events
  - View event registrations
  - Confirm attendance for students who actually attended events
  - Automatically notify relevant teachers if event timing overlaps with class timing

---

## Database Setup

- Update your database credentials in the code:
  ```java
  DriverManager.getConnection("jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME", "user", "password")
  ```
- Replace `YOUR_DATABASE_NAME` with your actual database name.
- The default username is `user` and password is `password` for sharing and testing purposes.

---

## Libraries

- The project uses the [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) JDBC driver for database connectivity.
- The JAR file is included in `lib/mysql-connector-j-9.3.0/`.
- **Source:** [MySQL Connector/J Official Download](https://dev.mysql.com/downloads/connector/j/)

---

## How to Run

1. **Clone the repository:**
   ```sh
   git clone https://github.com/yourusername/role-based-login-app.git
   ```
2. **Set up your MySQL database** and update the credentials in the code if needed.
3. **Compile the project:**
   ```sh
   javac -cp "lib/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar" -d bin src/ui/*.java src/services/*.java
   ```
4. **Run the app:**
   ```sh
   java -cp "bin;lib/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar" ui.Main
   ```
   *(Adjust the main class path as needed.)*

---

## Notes

- **bin/** and all compiled files are excluded from version control via `.gitignore`.
- No real credentials are stored in the repository.
- The JDBC driver in `lib/` is public and redistributable.

---

## License

This project is for educational use.  
MySQL Connector/J is © Oracle and used under its respective license.