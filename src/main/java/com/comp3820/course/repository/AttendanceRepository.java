package com.comp3820.course.repository;

import com.comp3820.course.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findAll();
    
    List<Attendance> findBySessionCode(String sessionCode);
    
    List<Attendance> findBySessionCodeOrderByAttendedAtDesc(String sessionCode);
    
    List<Attendance> findByUserIdOrderByAttendedAtDesc(Long userId);
    
    Optional<Attendance> findBySessionCodeAndUserId(String sessionCode, Long userId);
    
    Optional<Attendance> findBySessionCodeAndUserIsNull(String sessionCode);
    
    @Query("SELECT a FROM Attendance a WHERE a.sessionCode = :code AND a.user IS NULL AND a.active = true AND a.startTime <= CURRENT_TIMESTAMP AND a.endTime >= CURRENT_TIMESTAMP")
    Optional<Attendance> findActiveSessionByCode(@Param("code") String code);
}
