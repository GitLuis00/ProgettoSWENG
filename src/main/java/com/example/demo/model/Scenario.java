    package com.example.demo.model;

    import javax.persistence.*;
    import java.util.ArrayList;
    import java.util.List;

    @Entity
    public class Scenario {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(length = 1000)
        private String description;

        @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
        @JoinColumn(name = "scenario_id")
        private List<Choice> choices = new ArrayList<>();

        @ElementCollection
        private List<String> items = new ArrayList<>();

        @ManyToOne
        @JoinColumn(name = "collected_item_id")
        private Item collectedItem;

        private boolean isFinal;

        @OneToOne(cascade = CascadeType.ALL)
        @JoinColumn(name = "riddle_id")
        private Riddle riddle;

        public Scenario() {}

        public Scenario(String description) {
            this.description = description;
        }

        // Getters and Setters
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

        public List<Choice> getChoices() {
            return choices;
        }

        public void addChoice(Choice choice) {
            this.choices.add(choice);
        }

        public void addChoices(List<Choice> choices) {
            this.choices.addAll(choices);
        }

        public List<String> getItems() {
            return items;
        }

        public Item getCollectedItem() {
            return collectedItem;
        }

        public void setCollectedItem(Item collectedItem) {
            this.collectedItem = collectedItem;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public void setFinal(boolean aFinal) {
            isFinal = aFinal;
        }

        public Riddle getRiddle() {
            return riddle;
        }

        public void setRiddle(Riddle riddle) {
            this.riddle = riddle;
        }
    }
