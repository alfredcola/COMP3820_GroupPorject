package com.comp3820.course.repository;

import com.comp3820.course.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByUserId(Long userId);
    Optional<Vote> findByUserIdAndPollId(Long userId, Long pollId);
    List<Vote> findAll();
}
