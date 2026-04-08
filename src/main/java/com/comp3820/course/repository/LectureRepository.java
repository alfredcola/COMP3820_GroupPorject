package com.comp3820.course.repository;

import com.comp3820.course.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findAllByOrderByOrderIndexAsc();
}
