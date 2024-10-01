package com.example.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story")
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author; // Nuovo attributo

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Scenario startScenario;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Inventory inventory;

    private LocalDateTime creationDate; // Nuovo attributo

    public Story() {}

    public Story(String title, Scenario startScenario, Inventory inventory, String author) {
        this.title = title;
        this.startScenario = startScenario;
        this.inventory = inventory;
        this.author = author;
        this.creationDate = LocalDateTime.now(); // Imposta la data di creazione corrente
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Scenario getStartScenario() {
        return startScenario;
    }

    public void setStartScenario(Scenario startScenario) {
        this.startScenario = startScenario;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
