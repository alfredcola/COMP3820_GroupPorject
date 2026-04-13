package com.comp3820.course.controller;

import com.comp3820.course.model.*;
import com.comp3820.course.repository.*;
import com.comp3820.course.service.QRCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.net.URLEncoder;

@Controller
public class MainController {
    
    private final UserRepository userRepo;
    private final LectureRepository lectureRepo;
    private final PollRepository pollRepo;
    private final PollOptionRepository pollOptionRepo;
    private final VoteRepository voteRepo;
    private final CommentRepository commentRepo;
    private final AttendanceRepository attendanceRepo;
    private final PasswordEncoder passwordEncoder;
    private final QRCodeService qrCodeService;
    
    public MainController(UserRepository userRepo, LectureRepository lectureRepo,
                        PollRepository pollRepo, PollOptionRepository pollOptionRepo,
                        VoteRepository voteRepo, CommentRepository commentRepo,
                        AttendanceRepository attendanceRepo, PasswordEncoder passwordEncoder,
                        QRCodeService qrCodeService) {
        this.userRepo = userRepo;
        this.lectureRepo = lectureRepo;
        this.pollRepo = pollRepo;
        this.pollOptionRepo = pollOptionRepo;
        this.voteRepo = voteRepo;
        this.commentRepo = commentRepo;
        this.attendanceRepo = attendanceRepo;
        this.passwordEncoder = passwordEncoder;
        this.qrCodeService = qrCodeService;
    }
    
    // ==================== INDEX & NAVIGATION ====================
    
    @GetMapping("/")
    public String index(@RequestParam(name = "lang", required = false) String lang,
                       HttpServletRequest request,
                       Model model, Authentication auth) {
        if (lang != null) {
            request.getSession().setAttribute("lang", lang);
        }
        
        addLanguageToModel(request, model);
        
        List<Lecture> lectures = lectureRepo.findAllByOrderByOrderIndexAsc();
        List<Poll> polls = pollRepo.findAllByOrderByOrderIndexAsc();
        
        model.addAttribute("lectures", lectures);
        model.addAttribute("polls", polls);
        model.addAttribute("isLoggedIn", auth != null);
        
        if (auth != null) {
            User user = userRepo.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("isTeacher", user.getRole() == User.Role.TEACHER);
            }
        }
        
        return "index";
    }
    
    // ==================== LECTURE PAGES ====================
    
    @GetMapping("/lecture/{id}")
    public String lecture(@PathVariable Long id, Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        addLanguageToModel(request, model);
        
        Lecture lecture = lectureRepo.findById(id).orElse(null);
        if (lecture == null) return "redirect:/";
        
        List<Comment> comments = commentRepo.findByLectureId(id);
        
        model.addAttribute("lecture", lecture);
        model.addAttribute("comments", comments);
        model.addAttribute("isLoggedIn", auth != null);
        
        if (auth != null) {
            User user = userRepo.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("isTeacher", user.getRole() == User.Role.TEACHER);
            }
        }
        
        return "lecture";
    }
    
    // ==================== POLL PAGES ====================
    
    @GetMapping("/poll/{id}")
    public String poll(@PathVariable Long id, Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        addLanguageToModel(request, model);
        Poll poll = pollRepo.findById(id).orElse(null);
        if (poll == null) return "redirect:/";
        
        List<Comment> comments = commentRepo.findByPollId(id);
        
        User currentUser = null;
        Vote userVote = null;
        if (auth != null) {
            currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                userVote = voteRepo.findByUserIdAndPollId(currentUser.getId(), id).orElse(null);
                model.addAttribute("isTeacher", currentUser.getRole() == User.Role.TEACHER);
            }
        }
        
        model.addAttribute("poll", poll);
        model.addAttribute("comments", comments);
        model.addAttribute("isLoggedIn", auth != null);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userVote", userVote);
        int totalPollVotes = poll.getOptions().stream().mapToInt(PollOption::getVoteCount).sum();
        model.addAttribute("totalPollVotes", totalPollVotes);
        
        return "poll";
    }
    
    @PostMapping("/poll/{id}/vote")
    public String vote(@PathVariable Long id, @RequestParam Long optionId, Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        Poll poll = pollRepo.findById(id).orElse(null);
        if (poll == null) return "redirect:/";
        
        PollOption selectedOption = poll.getOptions().stream()
            .filter(o -> o.getId().equals(optionId))
            .findFirst().orElse(null);
        
        if (selectedOption == null) return "redirect:/poll/" + id;
        
        Vote existingVote = voteRepo.findByUserIdAndPollId(user.getId(), id).orElse(null);
        
        if (existingVote != null) {
            PollOption oldOption = existingVote.getOption();
            if (oldOption != null) {
                oldOption.setVoteCount(Math.max(0, oldOption.getVoteCount() - 1));
                pollOptionRepo.save(oldOption);
            }
            existingVote.setOption(selectedOption);
            existingVote.setVotedAt(LocalDateTime.now());
            voteRepo.save(existingVote);
        } else {
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setPoll(poll);
            vote.setOption(selectedOption);
            vote.setVotedAt(LocalDateTime.now());
            voteRepo.save(vote);
        }
        
        selectedOption.setVoteCount(selectedOption.getVoteCount() + 1);
        pollOptionRepo.save(selectedOption);
        
        return "redirect:/poll/" + id;
    }
    
    // ==================== AUTHENTICATION ====================
    
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        addLanguageToModel(request, model);
        return "login";
    }
    
    @GetMapping("/register")
    public String register(Model model, HttpServletRequest request) {
        addLanguageToModel(request, model);
        return "register";
    }
    
    @PostMapping("/register")
    public String registerSubmit(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String phone) {
        
        if (userRepo.existsByUsername(username)) {
            return "redirect:/register?error=exists";
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(User.Role.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        
        userRepo.save(user);
        
        return "redirect:/login?registered";
    }
    
    // ==================== PROFILE (Student Update Info) ====================
    
    @GetMapping("/profile")
    public String profile(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        addLanguageToModel(request, model);
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        model.addAttribute("user", user);
        return "profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String phone,
                              @RequestParam(required = false) String newPassword,
                              Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        
        userRepo.save(user);
        
        return "redirect:/profile?success";
    }
    
    // ==================== COMMENTS ====================
    
    @PostMapping("/comment/lecture/{lectureId}")
    public String commentLecture(@PathVariable Long lectureId,
                               @RequestParam String content,
                               Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        Lecture lecture = lectureRepo.findById(lectureId).orElse(null);
        if (lecture == null) return "redirect:/";
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setLecture(lecture);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepo.save(comment);
        
        return "redirect:/lecture/" + lectureId;
    }
    
    @PostMapping("/comment/poll/{pollId}")
    public String commentPoll(@PathVariable Long pollId,
                            @RequestParam String content,
                            Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        Poll poll = pollRepo.findById(pollId).orElse(null);
        if (poll == null) return "redirect:/";
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPoll(poll);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepo.save(comment);
        
        return "redirect:/poll/" + pollId;
    }
    
    @PostMapping("/comment/delete/{id}")
    public String deleteComment(@PathVariable Long id, Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        Comment comment = commentRepo.findById(id).orElse(null);
        if (comment == null) return "redirect:/";
        
        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isTeacher = user.getRole() == User.Role.TEACHER;
        
        if (!isOwner && !isTeacher) return "redirect:/";
        
        Long lectureId = comment.getLecture() != null ? comment.getLecture().getId() : null;
        Long pollId = comment.getPoll() != null ? comment.getPoll().getId() : null;
        
        commentRepo.delete(comment);
        
        if (lectureId != null) return "redirect:/lecture/" + lectureId;
        if (pollId != null) return "redirect:/poll/" + pollId;
        return "redirect:/";
    }
    
    // ==================== ADDITIONAL FEATURE 1: VOTING HISTORY ====================
    
    @GetMapping("/history/votes")
    public String voteHistory(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        addLanguageToModel(request, model);
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        List<Vote> votes = voteRepo.findByUserId(user.getId());
        
        model.addAttribute("votes", votes);
        model.addAttribute("currentUser", user);
        
        return "vote_history";
    }
    
    // ==================== ADDITIONAL FEATURE 2: COMMENT HISTORY ====================
    
    @GetMapping("/history/comments")
    public String commentHistory(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        addLanguageToModel(request, model);
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        List<Comment> comments = commentRepo.findByUserId(user.getId());
        
        model.addAttribute("comments", comments);
        model.addAttribute("currentUser", user);
        
        return "comment_history";
    }
    
    // ==================== TEACHER: MANAGE USERS ====================
    
    @GetMapping("/admin/users")
    public String manageUsers(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        model.addAttribute("isTeacher", true);
        model.addAttribute("currentUser", user);
        
        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);
        
        return "user_management";
    }
    
    @PostMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User teacher = userRepo.findByUsername(auth.getName()).orElse(null);
        if (teacher == null || teacher.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        if (teacher.getId().equals(id)) return "redirect:/admin/users";
        
        User targetUser = userRepo.findById(id).orElse(null);
        if (targetUser != null) {
            userRepo.delete(targetUser);
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/admin/user/add")
    public String addUser(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam String phone,
                         @RequestParam String role,
                         Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User teacher = userRepo.findByUsername(auth.getName()).orElse(null);
        if (teacher == null || teacher.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        if (userRepo.existsByUsername(username)) {
            return "redirect:/admin/users";
        }
        
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        try {
            newUser.setRole(User.Role.valueOf(role));
        } catch (Exception e) {
            newUser.setRole(User.Role.STUDENT);
        }
        newUser.setEnabled(true);
        newUser.setCreatedAt(LocalDateTime.now());
        
        userRepo.save(newUser);
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/admin/user/edit/{id}")
    public String editUser(@PathVariable Long id,
                          @RequestParam String fullName,
                          @RequestParam String email,
                          @RequestParam String phone,
                          Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User teacher = userRepo.findByUsername(auth.getName()).orElse(null);
        if (teacher == null || teacher.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        User targetUser = userRepo.findById(id).orElse(null);
        if (targetUser == null) {
            return "redirect:/admin/users";
        }
        
        targetUser.setFullName(fullName);
        targetUser.setEmail(email);
        targetUser.setPhone(phone);
        userRepo.save(targetUser);
        
        return "redirect:/admin/users";
    }
    
    // ==================== TEACHER: CREATE LECTURE ====================
    
    @GetMapping("/admin/lecture/create")
    public String createLecturePage(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        model.addAttribute("isTeacher", true);
        
        return "lecture_create";
    }
    
    @PostMapping("/admin/lecture/create")
    public String createLecture(@RequestParam String title,
                               @RequestParam String summary,
                               @RequestParam("file") MultipartFile file,
                               Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        try {
            String uploadDir = "uploads/lectures/";
            new File(uploadDir).mkdirs();
            
            String filename = null;
            String filePath = null;
            
            if (!file.isEmpty()) {
                filename = file.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                filePath = uploadDir + uuid + "_" + filename;
                Files.write(Paths.get(filePath), file.getBytes());
            }
            
            Lecture lecture = new Lecture();
            lecture.setTitle(title);
            lecture.setSummary(summary);
            lecture.setFilePath(filePath);
            lecture.setFileName(filename);
            lecture.setOrderIndex((int) (lectureRepo.count() + 1));
            lectureRepo.save(lecture);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "redirect:/";
    }
    
    @PostMapping("/admin/lecture/delete/{id}")
    public String deleteLecture(@PathVariable Long id, Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        Lecture lecture = lectureRepo.findById(id).orElse(null);
        if (lecture != null && lecture.getFilePath() != null) {
            try {
                Path filePath = Paths.get(lecture.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        lectureRepo.deleteById(id);
        return "redirect:/";
    }
    
    @GetMapping("/admin/lecture/import")
    public String importLecturesPage(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        model.addAttribute("isTeacher", true);
        
        File lectureNoteDir = new File("LectureNote");
        File[] files = lectureNoteDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf") || 
            name.toLowerCase().endsWith(".ppt") ||
            name.toLowerCase().endsWith(".pptx") ||
            name.toLowerCase().endsWith(".doc") ||
            name.toLowerCase().endsWith(".docx")
        );
        
        model.addAttribute("availableFiles", files);
        model.addAttribute("lectureNoteDir", lectureNoteDir.getAbsolutePath());
        
        return "lecture_import";
    }
    
    @PostMapping("/admin/lecture/import")
    public String importLectures(@RequestParam List<String> selectedFiles, Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        try {
            String lectureNoteDir = "LectureNote";
            String uploadDir = "uploads/lectures/";
            new File(uploadDir).mkdirs();
            
            int orderIndex = (int) lectureRepo.count() + 1;
            
            for (String fileName : selectedFiles) {
                File sourceFile = new File(lectureNoteDir, fileName);
                if (sourceFile.exists()) {
                    String uuid = UUID.randomUUID().toString();
                    String destFileName = uuid + "_" + fileName;
                    Path destPath = Paths.get(uploadDir, destFileName);
                    Files.copy(sourceFile.toPath(), destPath);
                    
                    String title = fileName.replaceAll("\\.(pdf|ppt|pptx|doc|docx)$", "");
                    title = title.replaceAll("[_-]", " ");
                    
                    Lecture lecture = new Lecture();
                    lecture.setTitle(title);
                    lecture.setSummary("Imported from " + fileName);
                    lecture.setFilePath(uploadDir + destFileName);
                    lecture.setFileName(fileName);
                    lecture.setOrderIndex(orderIndex++);
                    lectureRepo.save(lecture);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return "redirect:/?success=imported";
    }
    
    // ==================== TEACHER: CREATE POLL ====================
    
    @GetMapping("/admin/poll/create")
    public String createPollPage(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        model.addAttribute("isTeacher", true);
        
        return "poll_create";
    }
    
    @PostMapping("/admin/poll/create")
    public String createPoll(@RequestParam String question,
                            @RequestParam String option1,
                            @RequestParam String option2,
                            @RequestParam String option3,
                            @RequestParam String option4,
                            @RequestParam String option5,
                            Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        if (question == null || question.trim().isEmpty() ||
            option1 == null || option1.trim().isEmpty() ||
            option2 == null || option2.trim().isEmpty() ||
            option3 == null || option3.trim().isEmpty() ||
            option4 == null || option4.trim().isEmpty() ||
            option5 == null || option5.trim().isEmpty()) {
            return "redirect:/admin/poll/create?error=missingOptions";
        }
        
        Poll poll = new Poll();
        poll.setQuestion(question);
        poll.setOrderIndex((int) (pollRepo.count() + 1));
        
        java.util.List<PollOption> options = new java.util.ArrayList<>();
        
        for (String optText : List.of(option1, option2, option3, option4, option5)) {
            if (optText != null && !optText.trim().isEmpty()) {
                PollOption option = new PollOption();
                option.setText(optText);
                option.setVoteCount(0);
                option.setPoll(poll);
                options.add(option);
            }
        }
        
        poll.setOptions(options);
        pollRepo.save(poll);
        
        return "redirect:/";
    }
    
    @PostMapping("/admin/poll/delete/{id}")
    public String deletePoll(@PathVariable Long id, Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        Poll poll = pollRepo.findById(id).orElse(null);
        if (poll == null) return "redirect:/";
        
        int totalVotes = poll.getOptions().stream().mapToInt(PollOption::getVoteCount).sum();
        if (totalVotes > 0) {
            return "redirect:/poll/" + id + "?error=cannotDelete";
        }
        
        pollRepo.deleteById(id);
        return "redirect:/";
    }
    
    // ==================== ADDITIONAL FEATURE 3: BATCH UPLOAD ====================
    
    @GetMapping("/admin/upload")
    public String uploadPage(Authentication auth, HttpServletRequest request, Model model) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        model.addAttribute("isTeacher", true);
        
        return "upload";
    }
    
    @PostMapping("/admin/upload/batch")
    public String batchUpload(@RequestParam("files") MultipartFile[] files,
                            @RequestParam String title,
                            @RequestParam String summary,
                            Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        try {
            String uploadDir = "uploads/lectures/";
            new File(uploadDir).mkdirs();
            
            int order = (int) lectureRepo.count() + 1;
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path path = Paths.get(uploadDir, filename);
                    Files.write(path, file.getBytes());
                    
                    Lecture lecture = new Lecture();
                    lecture.setTitle(title + " - " + file.getOriginalFilename());
                    lecture.setSummary(summary);
                    lecture.setFilePath(uploadDir + filename);
                    lecture.setFileName(file.getOriginalFilename());
                    lecture.setOrderIndex(order++);
                    lectureRepo.save(lecture);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "redirect:/";
    }
    
    // ==================== ADDITIONAL FEATURE 4: MULTI-LANGUAGE ====================
    
    @GetMapping("/language/{lang}")
    public String changeLanguage(@PathVariable String lang,
                                @RequestParam(defaultValue = "/") String redirect,
                                HttpSession session,
                                Model model) {
        session.setAttribute("lang", lang);
        return "redirect:" + redirect;
    }
    
    private void addLanguageToModel(HttpServletRequest request, Model model) {
        String lang = (String) request.getSession().getAttribute("lang");
        model.addAttribute("currentLang", lang != null ? lang : "en");
    }
    
    // ==================== ATTENDANCE SYSTEM WITH QR CODE ====================
    
    @GetMapping("/admin/attendance")
    public String attendancePage(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        return "attendance";
    }
    
    @PostMapping("/admin/attendance/create")
    public String createAttendance(@RequestParam String sessionName,
                                   @RequestParam int durationMinutes,
                                   Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        Attendance attendance = new Attendance();
        attendance.setSessionName(sessionName);
        attendance.setSessionCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        attendance.setStartTime(LocalDateTime.now());
        attendance.setEndTime(LocalDateTime.now().plusMinutes(durationMinutes));
        attendance.setActive(true);
        
        attendanceRepo.save(attendance);
        
        return "redirect:/admin/attendance?code=" + attendance.getSessionCode();
    }
    
    @PostMapping("/admin/attendance/stop/{id}")
    public String stopAttendance(@PathVariable Long id, Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        Attendance attendance = attendanceRepo.findById(id).orElse(null);
        if (attendance != null) {
            attendance.setActive(false);
            attendanceRepo.save(attendance);
        }
        
        return "redirect:/admin/attendance";
    }
    
    @GetMapping("/admin/attendance/session/{code}")
    public String viewAttendanceSession(@PathVariable String code, Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        
        Attendance sessionInfo = attendanceRepo.findBySessionCodeAndUserIsNull(code).orElse(null);
        List<Attendance> allList = attendanceRepo.findBySessionCodeOrderByAttendedAtDesc(code);
        
        List<Attendance> studentAttendances = new java.util.ArrayList<>();
        
        if (allList != null) {
            for (Attendance att : allList) {
                if (att.getUser() != null && att.getUser().getRole() == User.Role.STUDENT) {
                    studentAttendances.add(att);
                }
            }
        }
        
        model.addAttribute("session", sessionInfo);
        model.addAttribute("attendances", studentAttendances);
        model.addAttribute("totalAttendees", studentAttendances.size());
        
        return "attendance_session";
    }
    
    @GetMapping("/admin/attendance/all")
    public String allAttendanceSessions(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        
        List<Attendance> allList = attendanceRepo.findAll();
        
        java.util.Map<String, java.util.List<Attendance>> sessionsByCode = new java.util.LinkedHashMap<>();
        java.util.Map<String, Boolean> sessionActiveStatus = new java.util.LinkedHashMap<>();
        java.util.Map<String, String> sessionNames = new java.util.LinkedHashMap<>();
        
        if (allList != null) {
            for (Attendance att : allList) {
                if (att.getSessionCode() != null && att.getUser() == null) {
                    sessionActiveStatus.put(att.getSessionCode(), att.isActive());
                    sessionNames.put(att.getSessionCode(), att.getSessionName());
                }
            }
            
            for (Attendance att : allList) {
                if (att.getSessionCode() != null) {
                    if (!sessionsByCode.containsKey(att.getSessionCode())) {
                        sessionsByCode.put(att.getSessionCode(), new java.util.ArrayList<>());
                    }
                    if (att.getUser() != null && att.getUser().getRole() == User.Role.STUDENT) {
                        sessionsByCode.get(att.getSessionCode()).add(att);
                    }
                }
            }
        }
        
        model.addAttribute("sessionsByCode", sessionsByCode);
        model.addAttribute("sessionActiveStatus", sessionActiveStatus);
        model.addAttribute("sessionNames", sessionNames);
        
        return "attendance_all";
    }
    
    // ==================== QR CODE GENERATION ENDPOINTS ====================
    
    /**
     * Generate QR Code for attendance session
     * The QR code contains the attendance join URL with the session code
     */
    @GetMapping("/admin/attendance/qr/{code}")
    public void getAttendanceQRCode(@PathVariable String code, 
                                    HttpServletResponse response,
                                    Authentication auth) {
        // Verify teacher authorization
        if (auth == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            // Verify session exists
            Attendance session = attendanceRepo.findBySessionCodeAndUserIsNull(code).orElse(null);
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Generate attendance URL (change localhost:8080 to your actual domain in production)
            String attendanceUrl = "http://localhost:8080/attendance/mark?code=" + code;
            
            // Generate QR code
            byte[] qrCode = qrCodeService.generateQRCodeImage(attendanceUrl);
            
            // Send response
            response.setContentType("image/png");
            response.setContentLength(qrCode.length);
            response.getOutputStream().write(qrCode);
            response.getOutputStream().flush();
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Generate QR Code with custom size
     */
    @GetMapping("/admin/attendance/qr/{code}/size/{size}")
    public void getAttendanceQRCodeWithSize(@PathVariable String code,
                                           @PathVariable int size,
                                           HttpServletResponse response,
                                           Authentication auth) {
        if (auth == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            Attendance session = attendanceRepo.findBySessionCodeAndUserIsNull(code).orElse(null);
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            String attendanceUrl = "http://localhost:8080/attendance/mark?code=" + code;
            byte[] qrCode = qrCodeService.generateQRCodeImage(attendanceUrl, size);
            
            response.setContentType("image/png");
            response.setContentLength(qrCode.length);
            response.getOutputStream().write(qrCode);
            response.getOutputStream().flush();
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    // ==================== STUDENT ATTENDANCE ====================
    
    @GetMapping("/attendance")
    public String studentAttendance(Model model, Authentication auth, HttpServletRequest request) {
        addLanguageToModel(request, model);
        model.addAttribute("isLoggedIn", auth != null);
        
        if (auth != null) {
            User user = userRepo.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                List<Attendance> myAttendance = attendanceRepo.findByUserIdOrderByAttendedAtDesc(user.getId());
                model.addAttribute("myAttendance", myAttendance);
                model.addAttribute("currentUser", user);
            }
        }
        
        return "attendance_student";
    }
    
    @PostMapping("/attendance/mark")
    public String markAttendance(@RequestParam String code, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/attendance?error=1";
        
        Attendance session = attendanceRepo.findActiveSessionByCode(code.toUpperCase()).orElse(null);
        if (session == null) {
            return "redirect:/attendance?error=2";
        }
        
        Attendance existing = attendanceRepo.findBySessionCodeAndUserId(code.toUpperCase(), user.getId()).orElse(null);
        if (existing != null) {
            return "redirect:/attendance?error=3";
        }
        
        Attendance attendance = new Attendance();
        attendance.setSessionCode(session.getSessionCode());
        attendance.setSessionName(session.getSessionName());
        attendance.setUser(user);
        attendance.setAttendedAt(LocalDateTime.now());
        attendance.setActive(true);
        attendance.setStartTime(session.getStartTime());
        attendance.setEndTime(session.getEndTime());
        
        attendanceRepo.save(attendance);
        
        return "redirect:/attendance?success=1";
    }
    
    @GetMapping("/attendance/mark")
    public String markAttendanceGet(@RequestParam String code, Model model, Authentication auth, HttpServletRequest request) {
        addLanguageToModel(request, model);
        
        if (auth == null) {
            model.addAttribute("error", "login");
            model.addAttribute("code", code);
            return "attendance_confirm";
        }
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            model.addAttribute("error", "user");
            model.addAttribute("code", code);
            return "attendance_confirm";
        }
        
        Attendance session = attendanceRepo.findActiveSessionByCode(code.toUpperCase()).orElse(null);
        if (session == null) {
            return "redirect:/attendance?error=2";
        }
        
        Attendance existing = attendanceRepo.findBySessionCodeAndUserId(code.toUpperCase(), user.getId()).orElse(null);
        if (existing != null) {
            return "redirect:/attendance?error=3";
        }
        
        model.addAttribute("session", session);
        model.addAttribute("code", code);
        model.addAttribute("currentUser", user);
        
        return "attendance_confirm";
    }
    
    // ==================== VIDEO STREAMING ====================
    
    @GetMapping("/video/{id}")
    public String videoPage(@PathVariable Long id, Model model, Authentication auth, HttpServletRequest request) {
        addLanguageToModel(request, model);
        
        Lecture lecture = lectureRepo.findById(id).orElse(null);
        if (lecture == null) return "redirect:/";
        
        model.addAttribute("lecture", lecture);
        model.addAttribute("isLoggedIn", auth != null);
        
        return "video";
    }
    
    @GetMapping("/stream/{id}")
    public void streamVideo(@PathVariable Long id, HttpServletResponse response, Authentication auth) {
        Lecture lecture = lectureRepo.findById(id).orElse(null);
        if (lecture == null || lecture.getFilePath() == null) return;
        
        try {
            Path videoPath = Paths.get(lecture.getFilePath());
            if (!Files.exists(videoPath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            long fileSize = Files.size(videoPath);
            String fileName = lecture.getFileName();
            
            String contentType = "video/mp4";
            if (fileName != null) {
                if (fileName.toLowerCase().endsWith(".webm")) {
                    contentType = "video/webm";
                } else if (fileName.toLowerCase().endsWith(".ogg")) {
                    contentType = "video/ogg";
                }
            }
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
            response.setHeader("Accept-Ranges", "bytes");
            
            String rangeHeader = response.getHeader("Range");
            
            if (rangeHeader == null) {
                response.setContentLengthLong(fileSize);
                Files.copy(videoPath, response.getOutputStream());
            } else {
                String[] ranges = rangeHeader.replace("bytes=", "").split("-");
                long start = 0;
                long end = fileSize - 1;
                
                if (ranges.length > 0 && !ranges[0].isEmpty()) {
                    start = Long.parseLong(ranges[0]);
                }
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
                
                long contentLength = end - start + 1;
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
                response.setContentLengthLong(contentLength);
                
                try (InputStream is = Files.newInputStream(videoPath)) {
                    byte[] buffer = new byte[8192];
                    long skipped = is.skip(start);
                    long remaining = contentLength;
                    
                    while (remaining > 0) {
                        int read = is.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (read == -1) break;
                        response.getOutputStream().write(buffer, 0, read);
                        remaining -= read;
                    }
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    // ==================== EDIT LECTURE ====================
    
    @GetMapping("/admin/lecture/edit/{id}")
    public String editLecturePage(@PathVariable Long id, Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        
        Lecture lecture = lectureRepo.findById(id).orElse(null);
        if (lecture == null) return "redirect:/";
        
        model.addAttribute("lecture", lecture);
        return "lecture_edit";
    }
    
    @PostMapping("/admin/lecture/edit/{id}")
    public String editLecture(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String summary,
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        Lecture lecture = lectureRepo.findById(id).orElse(null);
        if (lecture == null) return "redirect:/";
        
        lecture.setTitle(title);
        lecture.setSummary(summary);
        
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/lectures/";
                new File(uploadDir).mkdirs();
                
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + filename);
                Files.write(path, file.getBytes());
                
                lecture.setFilePath(uploadDir + filename);
                lecture.setFileName(file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        lectureRepo.save(lecture);
        return "redirect:/lecture/" + id;
    }
    
    // ==================== EDIT POLL ====================
    
    @GetMapping("/admin/poll/edit/{id}")
    public String editPollPage(@PathVariable Long id, Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        addLanguageToModel(request, model);
        model.addAttribute("isTeacher", true);
        
        Poll poll = pollRepo.findById(id).orElse(null);
        if (poll == null) return "redirect:/";
        
        model.addAttribute("poll", poll);
        return "poll_edit";
    }
    
    @PostMapping("/admin/poll/edit/{id}")
    public String editPoll(@PathVariable Long id,
                          @RequestParam String question,
                          @RequestParam String option1,
                          @RequestParam String option2,
                          @RequestParam String option3,
                          @RequestParam String option4,
                          @RequestParam String option5,
                          Authentication auth) {
        if (auth == null) return "redirect:/login";
        
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != User.Role.TEACHER) {
            return "redirect:/";
        }
        
        Poll poll = pollRepo.findById(id).orElse(null);
        if (poll == null) return "redirect:/";
        
        int totalVotes = poll.getOptions().stream().mapToInt(PollOption::getVoteCount).sum();
        if (totalVotes > 0) {
            return "redirect:/poll/" + id + "?error=cannotEdit";
        }
        
        poll.setQuestion(question);
        
        for (PollOption opt : poll.getOptions()) {
            pollOptionRepo.delete(opt);
        }
        poll.getOptions().clear();
        
        for (String optText : List.of(option1, option2, option3, option4, option5)) {
            if (optText != null && !optText.trim().isEmpty()) {
                PollOption option = new PollOption();
                option.setText(optText);
                option.setVoteCount(0);
                option.setPoll(poll);
                poll.getOptions().add(option);
            }
        }
        
        pollRepo.save(poll);
        return "redirect:/poll/" + id;
    }
}
