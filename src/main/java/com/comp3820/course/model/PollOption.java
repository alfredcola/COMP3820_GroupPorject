package com.comp3820.course.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class PollOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String text;
    private int voteCount;
    
    @ManyToOne
    @JoinColumn(name = "poll_id")
    @JsonIgnore
    private Poll poll;
    
    public PollOption() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }
    
    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }
}