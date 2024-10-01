package com.example.demo.services;

import com.example.demo.model.Story;
import com.example.demo.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    public List<Story> findAll() {
        return storyRepository.findAll();
    }

    public List<Story> findByAuthor(String author) {
        return storyRepository.findByAuthor(author);
    }

    public List<Story> findByCreationDateAfter(LocalDate startDate) {
        return storyRepository.findByCreationDateAfter(startDate);
    }

    public Optional<Story> findById(Long id) {
        return storyRepository.findById(id);
    }

    public Story save(Story story) {
        return storyRepository.save(story);
    }

    public void deleteById(Long id) {
        storyRepository.deleteById(id);
    }
}
