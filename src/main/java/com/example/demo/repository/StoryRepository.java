package com.example.demo.repository;

import com.example.demo.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByAuthor(String author);
    List<Story> findByCreationDateAfter(LocalDate startDate);
}
