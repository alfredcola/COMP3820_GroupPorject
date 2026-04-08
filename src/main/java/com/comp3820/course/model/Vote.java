package com.comp3820.course.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "poll_id")
    private Poll poll;
    
    @ManyToOne
    @JoinColumn(name = "option_id")
    private PollOption option;
    
    private LocalDateTime votedAt;
    
    public Vote() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }
    
    public PollOption getOption() { return option; }
    public void setOption(PollOption option) { this.option = option; }
    
    public LocalDateTime getVotedAt() { return votedAt; }
    public void setVotedAt(LocalDateTime votedAt) { this.votedAt = votedAt; }
}