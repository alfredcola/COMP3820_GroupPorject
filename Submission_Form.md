# COMP 3800SEF / 3820SEF / S380F - Group Project Submission Form

## Submission Date
**April 13, 2026**

---

## 1. Member List

| No. | Student ID | Full Name | Username | Role |
|-----|-----------|-----------|----------|------|
| 1 | [SID-001] | [Team Leader Name] | leader | Teacher/Student |
| 2 | [SID-002] | [Member 2 Name] | member2 | Student |
| 3 | [SID-003] | [Member 3 Name] | member3 | Student |
| 4 | [SID-004] | [Member 4 Name] | member4 | Student |
| 5 | [SID-005] | [Member 5 Name] | member5 | Student |

---

## 2. Project Information

**Project Theme:** Online Course Website

**Technologies Used:**
- Spring Boot 3.2.0
- Spring MVC
- Spring Security
- Spring Data JPA
- Thymeleaf (Template Engine)
- Bootstrap 5.3.0
- H2 Database
- Jakarta EE

---

## 3. Demo Video Link

**Video URL:** [INSERT GOOGLE DRIVE / YOUTUBE LINK HERE]

---

## 4. Database Information

| Item | Value |
|------|-------|
| Database Type | H2 Database |
| Database Name | coursedb |
| JDBC Connection | `jdbc:h2:./data/coursedb;AUTO_SERVER=TRUE` |
| Username | sa |
| Password | (empty) |
| H2 Console Path | /h2-console |

---

## 5. Default User Accounts

### Teacher Account

| Field | Value |
|-------|-------|
| Username | teacher |
| Password | password |
| Full Name | Dr. Hongli |
| Email | teacher@course.edu |
| Phone | 12345678 |
| Role | TEACHER |

### Student Account

| Field | Value |
|-------|-------|
| Username | student |
| Password | password |
| Full Name | Alfred Chen |
| Email | student@email.com |
| Phone | 98765432 |
| Role | STUDENT |

### Additional Accounts
> Create additional accounts via Admin Panel at `/admin/users`

---

## 6. Feature List

### Basic Features (All Implemented)

- [x] Index page with course name, description, lectures list, and polls list
- [x] Lecture pages with title, download links, summary, and comments
- [x] Poll pages with question, 5 MC options, vote counts, and comments
- [x] Student/Teacher registration and login
- [x] Unregistered users can read index page only
- [x] Registered students can comment, vote, update profile
- [x] Teachers can manage users, add/delete lectures and polls

### Additional Features (4 Implemented)

| # | Feature | URL |
|---|---------|-----|
| 1 | Voting History Page | `/history/votes` |
| 2 | Comment History Page | `/history/comments` |
| 3 | Batch File Upload | `/admin/upload` |
| 4 | Multiple Languages (EN/Chinese) | Language toggle in navbar |

### Bonus Features

- [x] Attendance System with QR code-style check-in
- [x] Video Streaming Support

---

## 7. How to Run the Application

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Gradle (or use the included gradlew wrapper)

### Steps

```bash
# 1. Navigate to project directory
cd comp3820-project

# 2. Set JAVA_HOME to JDK 17
export JAVA_HOME="/path/to/jdk-17"

# 3. Clean and build the project
./gradlew clean build

# 4. Run the application
./gradlew bootRun

# 5. Access the application at:
# http://localhost:8080
```

---

## 8. Project Structure

```
comp3820-project/
├── build.gradle                 # Gradle build configuration
├── settings.gradle              # Gradle settings
├── src/
│   └── main/
│       ├── java/com/comp3820/course/
│       │   ├── CourseApplication.java      # Main application class
│       │   ├── DataInitializer.java       # Initial data seeder
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── CustomUserDetailsService.java
│       │   ├── controller/
│       │   │   └── MainController.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Lecture.java
│       │   │   ├── Poll.java
│       │   │   ├── PollOption.java
│       │   │   ├── Comment.java
│       │   │   ├── Vote.java
│       │   │   └── Attendance.java
│       │   └── repository/
│       │       ├── UserRepository.java
│       │       ├── LectureRepository.java
│       │       ├── PollRepository.java
│       │       ├── PollOptionRepository.java
│       │       ├── VoteRepository.java
│       │       ├── CommentRepository.java
│       │       └── AttendanceRepository.java
│       └── resources/
│           ├── application.properties
│           ├── messages.properties         # English translations
│           ├── messages_zh.properties     # Chinese translations
│           ├── static/css/styles.css
│           └── templates/
│               ├── index.html
│               ├── login.html
│               ├── register.html
│               ├── lecture.html
│               ├── poll.html
│               ├── profile.html
│               ├── user_management.html
│               ├── lecture_create.html
│               ├── lecture_edit.html
│               ├── lecture_import.html
│               ├── poll_create.html
│               ├── poll_edit.html
│               ├── upload.html
│               ├── vote_history.html
│               ├── comment_history.html
│               ├── attendance*.html
│               ├── video.html
│               └── fragments/
│                   └── navbar.html
├── uploads/lectures/            # Uploaded lecture files
└── data/coursedb.mv.db         # H2 database file
```

---

## 9. Additional Notes

1. The H2 database is automatically created when the application first runs.
2. Initial data (teacher, student, 10 lectures, and 2 polls) is seeded automatically.
3. All file uploads are stored in the `uploads/lectures/` directory.
4. The database is stored in `data/coursedb.mv.db`.
5. To reset the database, stop the application and delete the `data/` folder.

---

**END OF SUBMISSION FORM**
