package com.example.demo.services;

import com.example.demo.model.Riddle;
import com.example.demo.repository.RiddleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RiddleService {

    @Autowired
    private RiddleRepository riddleRepository;

    public List<Riddle> findAll() {
        return riddleRepository.findAll();
    }

    public Optional<Riddle> findById(Long id) {
        return riddleRepository.findById(id);
    }

    public Riddle save(Riddle riddle) {
        return riddleRepository.save(riddle);
    }

    public void deleteById(Long id) {
        riddleRepository.deleteById(id);
    }
}
