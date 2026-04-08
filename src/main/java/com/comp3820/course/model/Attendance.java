package com.comp3820.course.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sessionCode;
    
    private String sessionName;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private boolean active;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    private LocalDateTime attendedAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
    
    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDateTime getAttendedAt() { return attendedAt; }
    public void setAttendedAt(LocalDateTime attendedAt) { this.attendedAt = attendedAt; }
}
