package com.example.together.repository;

import com.example.together.domain.Cafe;
import com.example.together.domain.CafeCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CafeCalendarRepository extends JpaRepository<CafeCalendar,Long> {
    List<CafeCalendar> findByCafe(Cafe cafe);

    Optional<CafeCalendar> findByMeetingId(Long meetingId);
}
