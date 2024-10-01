package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.services.InventoryService;
import com.example.demo.services.ItemService;
import com.example.demo.services.RiddleService;
import com.example.demo.services.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/scenarios")
public class ScenarioController {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RiddleService riddleService;

    @GetMapping
    public String listScenarios(Model model) {
        List<Scenario> scenarios = scenarioService.findAll();
        model.addAttribute("scenarios", scenarios);
        return "story_view";
    }

    @GetMapping("/new")
    public String showNewScenarioForm(Model model) {
        model.addAttribute("scenario", new Scenario());
        return "scenario_form";
    }

    @PostMapping
    public String saveScenario(@ModelAttribute("scenario") Scenario scenario) {
        scenarioService.save(scenario);
        return "redirect:/scenarios";
    }

    @GetMapping("/edit/{id}")
    public String showEditScenarioForm(@PathVariable("id") Long id, Model model) {
        logger.debug("Attempting to edit scenario with id: {}", id);
        Optional<Scenario> scenario = scenarioService.findById(id);
        if (scenario.isPresent()) {
            model.addAttribute("scenario", scenario.get());
            logger.debug("Scenario found: {}", scenario.get());
            return "scenario_form"; // Nome del template Thymeleaf
        } else {
            logger.warn("Scenario with id {} not found", id);
            return "redirect:/scenarios";
        }
    }

    @PostMapping("/update/{id}")
    public String updateScenario(@PathVariable("id") Long id, @ModelAttribute("scenario") Scenario updatedScenario, Model model) {
        logger.debug("Updating scenario with id: {}", id);
        Optional<Scenario> existingScenarioOpt = scenarioService.findById(id);

        if (existingScenarioOpt.isPresent()) {
            Scenario existingScenario = existingScenarioOpt.get();
            existingScenario.setDescription(updatedScenario.getDescription());
            scenarioService.save(existingScenario);
            logger.debug("Scenario updated: {}", existingScenario);
            model.addAttribute("message", "Scenario updated successfully");
        } else {
            logger.warn("Scenario with id {} not found", id);
            model.addAttribute("message", "Scenario not found");
        }

        model.addAttribute("scenario", updatedScenario);
        return "scenario_form";
    }


    @GetMapping("/delete/{id}")
    public String deleteScenario(@PathVariable("id") Long id) {
        logger.debug("Deleting scenario with id: {}", id);
        scenarioService.delete(id);
        logger.debug("Scenario deleted with id: {}", id);
        return "redirect:/scenarios";
    }

    @GetMapping("/view/{id}")
    public String viewScenario(@PathVariable("id") Long id, Model model) {
        logger.debug("Viewing scenario with id: {}", id);
        Optional<Scenario> scenario = scenarioService.findById(id);
        if (scenario.isPresent()) {
            model.addAttribute("scenario", scenario.get());
            logger.debug("Scenario found: {}", scenario.get());
            return "scenario_view";
        } else {
            logger.warn("Scenario with id {} not found", id);
            return "redirect:/scenarios";
        }
    }

    @GetMapping("/{id}/addChoice")
    public String showAddChoiceForm(@PathVariable("id") Long id, Model model) {
        logger.debug("Attempting to add choice to scenario with id: {}", id);
        Optional<Scenario> scenario = scenarioService.findById(id);
        if (scenario.isPresent()) {
            if (scenario.get().isFinal()) {
                model.addAttribute("error", "Cannot add choices to a final scenario.");
                model.addAttribute("scenario", scenario.get());
                logger.warn("Attempted to add choice to final scenario with id: {}", id);
                return "scenario_view"; // Redirect to the scenario view with an error message
            }
            Choice choice = new Choice();
            choice.setNextScenario(new Scenario());
            model.addAttribute("scenario", scenario.get());
            model.addAttribute("choice", choice);

            // Adding the list of items to the model
            List<Item> items = itemService.findAll();
            model.addAttribute("items", items);

            logger.debug("Adding choice form to scenario with id: {}", id);
            return "choice_form";
        } else {
            logger.warn("Scenario with id {} not found", id);
            return "redirect:/scenarios";
        }
    }

    @PostMapping("/{id}/addChoice")
    public String addChoice(@PathVariable("id") Long id, @ModelAttribute("choice") Choice choice,
                            @RequestParam("collectedItemId") Long collectedItemId) {
        logger.debug("Adding choice to scenario with id: {}", id);
        Optional<Scenario> scenarioOpt = scenarioService.findById(id);
        if (scenarioOpt.isPresent()) {
            Scenario scenario = scenarioOpt.get();

            // Save the nextScenario first
            Scenario nextScenario = choice.getNextScenario();
            scenarioService.save(nextScenario);

            // Now save the choice with the saved nextScenario
            choice.setNextScenario(nextScenario);
            scenario.addChoice(choice);
            scenarioService.save(scenario);

            // Add collected item to the scenario
            if (collectedItemId != null && collectedItemId > 0) {
                Optional<Item> collectedItemOpt = itemService.findById(collectedItemId);
                if (collectedItemOpt.isPresent()) {
                    Item collectedItem = collectedItemOpt.get();
                    nextScenario.setCollectedItem(collectedItem);
                    scenarioService.save(nextScenario);
                    logger.debug("Collected item added to next scenario with id: {}", nextScenario.getId());
                }
            }
        }
        return "redirect:/scenarios/view/" + id;
    }

    @GetMapping("/riddle/new/{scenarioId}")
    public String showAddRiddleForm(@PathVariable("scenarioId") Long scenarioId, Model model) {
        logger.debug("Attempting to add riddle to scenario with id: {}", scenarioId);
        Optional<Scenario> scenarioOpt = scenarioService.findById(scenarioId);
        if (scenarioOpt.isPresent()) {
            Scenario scenario = scenarioOpt.get();
            model.addAttribute("scenario", scenario);
            model.addAttribute("riddle", new Riddle());
            logger.debug("Adding riddle form to scenario with id: {}", scenarioId);
            return "riddle_form";
        } else {
            logger.warn("Scenario with id {} not found", scenarioId);
            return "redirect:/error";
        }
    }

    @PostMapping("/riddle/save/{scenarioId}")
    public String saveRiddle(@PathVariable("scenarioId") Long scenarioId, @ModelAttribute("riddle") Riddle riddle) {
        logger.debug("Saving riddle for scenario with id: {}", scenarioId);
        Optional<Scenario> scenarioOpt = scenarioService.findById(scenarioId);
        if (scenarioOpt.isPresent()) {
            Riddle savedRiddle = riddleService.save(riddle);
            Scenario scenario = scenarioOpt.get();
            scenario.setRiddle(savedRiddle);
            scenarioService.save(scenario);
            logger.debug("Riddle saved for scenario with id: {}", scenarioId);
            return "redirect:/scenarios/view/" + scenarioId;
        } else {
            logger.warn("Scenario with id {} not found", scenarioId);
            return "redirect:/error";
        }
    }
}
