package com.example.demo.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long storyId;

    @Enumerated(EnumType.STRING)
    private GameSessionStatus status;

    private Long currentScenarioId;

    @ElementCollection
    private List<Long> visitedScenarios = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    private boolean riddleResolved; // Aggiunto questo campo

    public GameSession() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public GameSessionStatus getStatus() {
        return status;
    }

    public void setStatus(GameSessionStatus status) {
        this.status = status;
    }

    public Long getCurrentScenarioId() {
        return currentScenarioId;
    }

    public void setCurrentScenarioId(Long currentScenarioId) {
        this.currentScenarioId = currentScenarioId;
    }

    public List<Long> getVisitedScenarios() {
        return visitedScenarios;
    }

    public void setVisitedScenarios(List<Long> visitedScenarios) {
        this.visitedScenarios = visitedScenarios;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public boolean isRiddleResolved() {
        return riddleResolved;
    }

    public void setRiddleResolved(boolean riddleResolved) {
        this.riddleResolved = riddleResolved;
    }
}
