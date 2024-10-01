package com.example.demo.repository;

import com.example.demo.model.Riddle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiddleRepository extends JpaRepository<Riddle, Long> {
}
