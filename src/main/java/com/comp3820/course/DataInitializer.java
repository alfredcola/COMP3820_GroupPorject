package com.comp3820.course;

import com.comp3820.course.model.*;
import com.comp3820.course.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepo;
    private final LectureRepository lectureRepo;
    private final PollRepository pollRepo;
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializer(UserRepository userRepo, LectureRepository lectureRepo, 
                          PollRepository pollRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.lectureRepo = lectureRepo;
        this.pollRepo = pollRepo;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) {
        if (userRepo.count() == 0) {
            User teacher = new User();
            teacher.setUsername("teacher");
            teacher.setPassword(passwordEncoder.encode("password"));
            teacher.setFullName("Dr. Hongli");
            teacher.setEmail("teacher@course.edu");
            teacher.setPhone("12345678");
            teacher.setRole(User.Role.TEACHER);
            teacher.setCreatedAt(LocalDateTime.now());
            userRepo.save(teacher);
            
            User student = new User();
            student.setUsername("student");
            student.setPassword(passwordEncoder.encode("password"));
            student.setFullName("Alfred Chen");
            student.setEmail("student@email.com");
            student.setPhone("98765432");
            student.setRole(User.Role.STUDENT);
            student.setCreatedAt(LocalDateTime.now());
            userRepo.save(student);
        }
        
        if (lectureRepo.count() == 0) {
            String[][] lectures = {
                {"Lecture 1: Introduction to Web Development", "Basic concepts of web development, HTTP protocol, HTML, CSS", "uploads/lectures/Lecture 1 update.pdf"},
                {"Lecture 2: Java Servlets & JSP", "Servlet lifecycle, request/response handling, session management", "uploads/lectures/Lecture02.pdf"},
                {"Lecture 3: Spring MVC Framework", "Controller, Model, View, Thymeleaf templates", "uploads/lectures/Lecture 3 notes.pdf"},
                {"Lecture 4: Spring Boot & Database", "JPA, H2 database, CRUD operations", "uploads/lectures/Lecture 4 notes.pdf"},
                {"Lecture 5: Spring Security", "Authentication, Authorization, Role-based access", "uploads/lectures/Lecture05.pdf"},
                {"Lecture 6: REST APIs", "RESTful services, JSON, AJAX", "uploads/lectures/Lecture 6.pdf"},
                {"Lecture 7: Client-Side Scripting", "JavaScript, DOM manipulation, AJAX", "uploads/lectures/Lecture 7.pdf"},
                {"Lecture 8: Web Services", "SOAP vs REST, WSDL, SOA concepts", "uploads/lectures/Lecture 8.pdf"},
                {"Lecture 9: Cloud Computing", "IaaS, PaaS, SaaS, Deployment", "uploads/lectures/Lecture09.pdf"},
                {"Lecture 10: Course Review", "Summary and exam preparation", "uploads/lectures/Lecture 10 notes.pdf"}
            };
            
            for (int i = 0; i < lectures.length; i++) {
                Lecture lecture = new Lecture();
                lecture.setTitle(lectures[i][0]);
                lecture.setSummary(lectures[i][1]);
                lecture.setFilePath(lectures[i][2]);
                lecture.setFileName(lectures[i][2].replace("uploads/lectures/", ""));
                lecture.setOrderIndex(i + 1);
                lectureRepo.save(lecture);
            }
        }
        
        if (pollRepo.count() == 0) {
            String[] questions = {
                "Which topic should we cover next?",
                "How do you prefer to learn?"
            };
            
            String[][][] options = {
                {
                    {"Machine Learning", "Cloud Computing", "Mobile Apps", "DevOps", "Cybersecurity"},
                },
                {
                    {"Video Tutorials", "Hands-on Labs", "Reading Materials", "Group Projects", "Online Quizzes"},
                }
            };
            
            for (int i = 0; i < questions.length; i++) {
                Poll poll = new Poll();
                poll.setQuestion(questions[i]);
                poll.setOrderIndex(i + 1);
                
                List<PollOption> pollOptions = new java.util.ArrayList<>();
                for (String optText : options[i][0]) {
                    PollOption option = new PollOption();
                    option.setText(optText);
                    option.setVoteCount(0);
                    option.setPoll(poll);
                    pollOptions.add(option);
                }
                poll.setOptions(pollOptions);
                pollRepo.save(poll);
            }
        }
    }
}