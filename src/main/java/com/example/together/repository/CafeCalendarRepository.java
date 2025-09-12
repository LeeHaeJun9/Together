package com.example.together.repository;

import com.example.together.domain.CafeCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeCalendarRepository extends JpaRepository<CafeCalendar,Long> {
}
