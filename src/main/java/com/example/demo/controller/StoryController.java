package com.example.demo.controller;

import com.example.demo.model.Choice;
import com.example.demo.model.Inventory;
import com.example.demo.model.Scenario;
import com.example.demo.model.Story;
import com.example.demo.services.InventoryService;
import com.example.demo.services.ScenarioService;
import com.example.demo.services.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public String listStories(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            List<Story> stories = storyService.findByAuthor(username);
            model.addAttribute("stories", stories);
            model.addAttribute("username", username);
        } else {
            model.addAttribute("stories", new ArrayList<>());
        }
        return "story_list";
    }

    @GetMapping("/new")
    public String showNewStoryForm(Model model, @SessionAttribute("username") String username) {
        model.addAttribute("username", username);
        model.addAttribute("story", new Story());
        model.addAttribute("scenario", new Scenario());
        return "story_form";
    }

    @PostMapping
    public String saveStory(@ModelAttribute("story") Story story, HttpSession session) {
        // Assicurati che lo scenario iniziale sia popolato e salvato correttamente
        Scenario startScenario = story.getStartScenario();
        if (startScenario != null) {
            scenarioService.save(startScenario);
        }

        // Creazione di un nuovo inventario associato alla storia
        Inventory inventory = new Inventory("Inventory for " + story.getTitle(), 0);
        inventoryService.save(inventory);
        story.setInventory(inventory);

        // Impostare l'autore dalla variabile di sessione
        String username = (String) session.getAttribute("username");
        story.setAuthor(username);

        // Impostare la data di creazione
        story.setCreationDate(LocalDateTime.now());

        storyService.save(story);
        return "redirect:/stories";
    }

    @PostMapping("/update/{id}")
    public String updateStory(@PathVariable("id") Long id, @ModelAttribute("story") Story story, @ModelAttribute("scenario") Scenario scenario) {
        scenario.setId(story.getStartScenario().getId());
        scenarioService.save(scenario);
        story.setStartScenario(scenario);
        story.setId(id);
        storyService.save(story);
        return "redirect:/stories";
    }

    @GetMapping("/delete/{id}")
    public String deleteStory(@PathVariable("id") Long id) {
        Optional<Story> story = storyService.findById(id);
        if (story.isPresent()) {
            Story foundStory = story.get();
            if (foundStory.getStartScenario() != null) {
                scenarioService.delete(foundStory.getStartScenario().getId());
            }
            if (foundStory.getInventory() != null) {
                inventoryService.deleteById(foundStory.getInventory().getId());
            }
            storyService.deleteById(id);
        }
        return "redirect:/stories";
    }

    @GetMapping("/viewStories")
    public String viewStories(
            @RequestParam(name = "username", required = false) String username,
            Model model) {

        List<Story> stories;

        if (username != null && !username.trim().isEmpty()) {
            // Filtra solo per autore
            stories = storyService.findByAuthor(username);
        } else {
            // Mostra tutte le storie se nessun filtro è applicato
            stories = storyService.findAll();
        }

        model.addAttribute("stories", stories);
        return "catalog";
    }


    @GetMapping("/view/{id}")
    public String viewStory(@PathVariable("id") Long id, Model model, @SessionAttribute("username") String username) {
        Optional<Story> story = storyService.findById(id);
        if (story.isPresent()) {
            Story foundStory = story.get();
            Scenario rootScenario = foundStory.getStartScenario();
            List<Scenario> scenarios = new ArrayList<>();
            gatherScenarios(rootScenario, scenarios);
            model.addAttribute("username", username);
            model.addAttribute("story", foundStory);
            model.addAttribute("rootScenario", rootScenario);
            model.addAttribute("scenarios", scenarios);
            return "story_view";
        } else {
            return "redirect:/stories";
        }
    }

    private void gatherScenarios(Scenario scenario, List<Scenario> scenarios) {
        if (scenario != null && !scenarios.contains(scenario)) {
            scenarios.add(scenario);
            for (Choice choice : scenario.getChoices()) {
                gatherScenarios(choice.getNextScenario(), scenarios);
            }
        }
    }
}
