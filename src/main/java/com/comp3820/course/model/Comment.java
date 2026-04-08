package com.comp3820.course.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 1000)
    private String content;
    
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "lecture_id")
    @JsonIgnore
    private Lecture lecture;
    
    @ManyToOne
    @JoinColumn(name = "poll_id")
    @JsonIgnore
    private Poll poll;
    
    public Comment() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Lecture getLecture() { return lecture; }
    public void setLecture(Lecture lecture) { this.lecture = lecture; }
    
    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }
}