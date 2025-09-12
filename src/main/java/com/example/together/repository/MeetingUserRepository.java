package com.example.together.repository;

import com.example.together.domain.MeetingUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingUserRepository extends JpaRepository<MeetingUser,Long> {
}
