package com.comp3820.course.repository;

import com.comp3820.course.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findAllByOrderByOrderIndexAsc();
}
