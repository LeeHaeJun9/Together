package com.example.together.repository;

import com.example.together.domain.Meeting;
import com.example.together.repository.search.MeetingSearch;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting,Long>, MeetingSearch{
}
