package com.example.demo.repository;

import com.example.demo.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
    public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    Optional<Scenario> findByRiddle_Id(Long riddleId);
    }

