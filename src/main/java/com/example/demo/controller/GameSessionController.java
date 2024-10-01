package com.example.demo.controller;

import com.example.demo.CustomUserDetails;
import com.example.demo.model.GameSession;
import com.example.demo.model.Riddle;
import com.example.demo.model.Scenario;
import com.example.demo.model.Story;
import com.example.demo.services.GameSessionService;
import com.example.demo.services.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gamesession")
public class GameSessionController {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private StoryService storyService;

    @PostMapping("/start")
    public String startSession(@RequestParam Long storyId, Model model) {
        Long userId = getCurrentUserId();
        GameSession session = gameSessionService.startSession(userId, storyId);
        model.addAttribute("sessionId", session.getId());
        return "redirect:/gamesession/play?sessionId=" + session.getId();
    }

    @PostMapping("/endAndStartNew")
    public String endAndStartNewSession(@RequestParam Long currentSessionId, @RequestParam Long storyId, Model model) {
        gameSessionService.endSession(currentSessionId);
        Long userId = getCurrentUserId();
        GameSession session = gameSessionService.startSession(userId, storyId);
        model.addAttribute("sessionId", session.getId());
        return "redirect:/gamesession/play?sessionId=" + session.getId();
    }

    @GetMapping("/play")
    public String playGame(@RequestParam Long sessionId, Model model) {
        Scenario scenario = gameSessionService.getCurrentScenario(sessionId);
        GameSession session = gameSessionService.getSession(sessionId).orElse(null);
        if (session != null) {
            model.addAttribute("inventory", session.getInventory());
            model.addAttribute("visitedScenarios", session.getVisitedScenarios());
            model.addAttribute("isFinalScenario", scenario.isFinal());
            model.addAttribute("riddleResolved", session.isRiddleResolved()); // Aggiungi riddleResolved al modello
        }
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("scenario", scenario);
        return "game";
    }

    @GetMapping("/list")
    public String listStories(Model model) {
        List<Story> stories = storyService.findAll();
        Long userId = getCurrentUserId();
        Map<Long, GameSession> ongoingSessions = new HashMap<>();
        for (Story story : stories) {
            gameSessionService.getOngoingSession(userId, story.getId()).ifPresent(session -> ongoingSessions.put(story.getId(), session));
        }
        model.addAttribute("stories", stories);
        model.addAttribute("userId", userId);
        model.addAttribute("ongoingSessions", ongoingSessions);
        return "game_list";
    }

    @PostMapping("/choose")
    public String chooseOption(@RequestParam Long sessionId, @RequestParam Long choiceId, Model model) {
        Object[] result = gameSessionService.processChoice(sessionId, choiceId);
        Scenario nextScenario = (Scenario) result[0];
        boolean hasRiddle = (Boolean) result[1];

        if (nextScenario != null) {
            if (hasRiddle) {
                model.addAttribute("riddle", nextScenario.getRiddle());
                model.addAttribute("sessionId", sessionId);
                model.addAttribute("scenario", nextScenario);
                return "riddle";
            } else {
                return "redirect:/gamesession/play?sessionId=" + sessionId;
            }
        } else {
            model.addAttribute("errorMessage", "Oggetto mancante nell'inventario.");
            Scenario currentScenario = gameSessionService.getCurrentScenario(sessionId);
            GameSession session = gameSessionService.getSession(sessionId).orElse(null);
            if (session != null) {
                model.addAttribute("inventory", session.getInventory());
                model.addAttribute("visitedScenarios", session.getVisitedScenarios());
                model.addAttribute("isFinalScenario", currentScenario.isFinal());
            }
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("scenario", currentScenario);
            return "game";
        }
    }

    @PostMapping("/answerRiddle")
    public String answerRiddle(@RequestParam Long sessionId, @RequestParam Long riddleId, @RequestParam String answer, Model model) {
        boolean isCorrect = gameSessionService.checkRiddleAnswer(sessionId, riddleId, answer);
        GameSession session = gameSessionService.getSession(sessionId).orElse(null);
        if (session != null) {
            session.setRiddleResolved(isCorrect);
            gameSessionService.updateSession(session);
        }
        if (isCorrect) {
            return "redirect:/gamesession/play?sessionId=" + sessionId;
        } else {
            model.addAttribute("errorMessage", "Incorrect answer. Try again.");
            Riddle riddle = gameSessionService.getRiddleById(riddleId);
            Scenario scenario = gameSessionService.getCurrentScenario(sessionId);
            model.addAttribute("riddle", riddle);
            model.addAttribute("scenario", scenario);
            model.addAttribute("sessionId", sessionId);
            return "riddle";
        }
    }

    @PostMapping("/collectItem")
    public String collectItem(@RequestParam Long sessionId) {
        gameSessionService.collectItem(sessionId);
        return "redirect:/gamesession/play?sessionId=" + sessionId;
    }

    @PostMapping("/saveAndExit")
    public String saveAndExit(@RequestParam Long sessionId) {
        return "redirect:/gamesession/list";
    }

    @PostMapping("/endSession")
    public String endSession(@RequestParam Long sessionId) {
        gameSessionService.endSession(sessionId);
        return "redirect:/gamesession/list";
    }

    @GetMapping("/summary")
    public String showSummary(@RequestParam Long sessionId, Model model) {
        GameSession session = gameSessionService.getSession(sessionId).orElse(null);
        if (session != null) {
            List<Scenario> visitedScenarios = gameSessionService.getVisitedScenarios(session);
            model.addAttribute("visitedScenarios", visitedScenarios);
        }
        return "story_summary";
    }

    @GetMapping("/api/user/{userId}")
    @ResponseBody
    public List<GameSession> getUserSessions(@PathVariable Long userId) {
        return gameSessionService.getUserSessions(userId);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return ((CustomUserDetails) userDetails).getUser().getId();
        }
        return null;
    }
}
