package com.example.demo.repository;

import com.example.demo.model.PremiumUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumUserRepository extends JpaRepository<PremiumUser, Long> {
}
