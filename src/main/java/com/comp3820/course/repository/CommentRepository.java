package com.comp3820.course.repository;

import com.comp3820.course.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByUserId(Long userId);
    List<Comment> findByLectureId(Long lectureId);
    List<Comment> findByPollId(Long pollId);
}
