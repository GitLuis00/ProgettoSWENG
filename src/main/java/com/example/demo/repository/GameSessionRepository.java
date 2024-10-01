package com.example.demo.repository;

import com.example.demo.model.GameSession;
import com.example.demo.model.GameSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    // Trova tutte le sessioni di gioco di un determinato utente
    List<GameSession> findByUserId(Long userId);
    Optional<GameSession> findByUserIdAndStoryIdAndStatus(Long userId, Long storyId, GameSessionStatus status);
}
