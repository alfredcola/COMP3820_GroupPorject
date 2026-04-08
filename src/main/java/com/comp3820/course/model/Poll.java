package com.comp3820.course.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String question;
    private int orderIndex;
    
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    private List<PollOption> options;
    
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    private List<Comment> comments;
    
    public Poll() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    
    public List<PollOption> getOptions() { return options; }
    public void setOptions(List<PollOption> options) { this.options = options; }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}