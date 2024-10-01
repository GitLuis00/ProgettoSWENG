package com.example.demo.model;
        import javax.persistence.*;
@Entity
@Table(name = "choice")
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String description;
    @ManyToOne
    @JoinColumn(name = "next_scenario_id")
    private Scenario nextScenario;
    @ManyToOne
    @JoinColumn(name = "required_item_id", nullable = true)
    private Item requiredItem;
    // Getters and setters...
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Scenario getNextScenario() {
        return nextScenario;
    }
    public void setNextScenario(Scenario nextScenario) {
        this.nextScenario = nextScenario;
    }
    public Item getRequiredItem() {
        return requiredItem;
    }
    public void setRequiredItem(Item requiredItem) {
        this.requiredItem = requiredItem;
    }
}
