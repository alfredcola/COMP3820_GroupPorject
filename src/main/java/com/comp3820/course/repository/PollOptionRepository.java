package com.comp3820.course.repository;

import com.comp3820.course.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
}
