package com.example.demo.services;

import com.example.demo.model.*;
import com.example.demo.repository.GameSessionRepository;
import com.example.demo.repository.ScenarioRepository;
import com.example.demo.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class GameSessionService {

    private static final Logger logger = Logger.getLogger(GameSessionService.class.getName());

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private StoryRepository storyRepository;

    // Inizia una nuova sessione di gioco
    public GameSession startSession(Long userId, Long storyId) {
        GameSession session = new GameSession();
        session.setUserId(userId);
        session.setStoryId(storyId);
        session.setStatus(GameSessionStatus.IN_PROGRESS);
        Long firstScenarioId = getFirstScenarioId(storyId);
        session.setCurrentScenarioId(firstScenarioId);
        session.setVisitedScenarios(List.of(firstScenarioId));
        session.setInventory(new Inventory("User Inventory", 10)); // Inizializza l'inventario con un nome e una capacità di esempio
        return gameSessionRepository.save(session);
    }

    // Recupera una sessione di gioco tramite ID
    public Optional<GameSession> getSession(Long sessionId) {
        return gameSessionRepository.findById(sessionId);
    }

    // Recupera tutte le sessioni di gioco di un determinato utente
    public List<GameSession> getUserSessions(Long userId) {
        return gameSessionRepository.findByUserId(userId);
    }

    // Aggiorna una sessione di gioco
    public GameSession updateSession(GameSession session) {
        return gameSessionRepository.save(session);
    }

    // Elimina una sessione di gioco
    public void deleteSession(Long sessionId) {
        gameSessionRepository.deleteById(sessionId);
    }

    // Ottiene il primo scenario di una storia
    private Long getFirstScenarioId(Long storyId) {
        Optional<Story> storyOpt = storyRepository.findById(storyId);
        if (storyOpt.isPresent()) {
            Story story = storyOpt.get();
            return story.getStartScenario().getId();
        }
        return null;
    }

    // Recupera lo scenario attuale della sessione di gioco
    public Scenario getCurrentScenario(Long sessionId) {
        Optional<GameSession> sessionOpt = gameSessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            Scenario scenario = scenarioRepository.findById(session.getCurrentScenarioId()).orElse(null);
            if (scenario != null && scenario.getRiddle() != null) {
                logger.info("Current scenario contains a riddle: " + scenario.getRiddle().getQuestion());
            } else {
                logger.info("Current scenario does not contain a riddle.");
            }
            return scenario;
        }
        return null;
    }

    // Verifica se è possibile navigare a un determinato scenario tramite la scelta
    public boolean canNavigateToScenario(GameSession session, Choice chosenChoice) {
        // Se l'oggetto richiesto ha l'ID uguale a 1, permette la navigazione
        if (chosenChoice.getRequiredItem() != null && chosenChoice.getRequiredItem().getId() == 1) {
            return true;
        }
        // Altrimenti, verifica se l'oggetto è presente nell'inventario
        if (chosenChoice.getRequiredItem() != null &&
                !session.getInventory().getItems().contains(chosenChoice.getRequiredItem())) {
            logger.warning("Required item " + chosenChoice.getRequiredItem().getName() + " not found in inventory.");
            return false;
        }
        return true;
    }
    // Adjust processChoice to return an indication if the next scenario has a riddle
    public Object[] processChoice(Long sessionId, Long choiceId) {
        try {
            Optional<GameSession> sessionOpt = gameSessionRepository.findById(sessionId);
            if (sessionOpt.isPresent()) {
                GameSession session = sessionOpt.get();
                Scenario currentScenario = scenarioRepository.findById(session.getCurrentScenarioId()).orElse(null);
                if (currentScenario != null) {
                    Choice chosenChoice = currentScenario.getChoices().stream()
                            .filter(choice -> choice.getId().equals(choiceId))
                            .findFirst()
                            .orElse(null);
                    if (chosenChoice != null) {
                        Scenario nextScenario = chosenChoice.getNextScenario();
                        if (canNavigateToScenario(session, chosenChoice)) {
                            session.setCurrentScenarioId(nextScenario.getId());
                            session.getVisitedScenarios().add(nextScenario.getId());
                            if (nextScenario.isFinal()) {
                                session.setStatus(GameSessionStatus.COMPLETED);
                            }
                            gameSessionRepository.save(session);
                            return new Object[]{nextScenario, nextScenario.getRiddle() != null};
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error processing choice: " + e.getMessage());
        }
        return new Object[]{null, false};
    }

    // Metodo per raccogliere l'oggetto nello scenario corrente
    public void collectItem(Long sessionId) {
        Optional<GameSession> sessionOpt = gameSessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            Scenario currentScenario = scenarioRepository.findById(session.getCurrentScenarioId()).orElse(null);
            if (currentScenario != null && currentScenario.getCollectedItem() != null) {
                try {
                    session.getInventory().addItem(currentScenario.getCollectedItem());
                    logger.info("Added item to inventory: " + currentScenario.getCollectedItem().getName());
                    gameSessionRepository.save(session);
                } catch (Exception e) {
                    logger.severe("Error adding item to inventory: " + e.getMessage());
                }
            }
        }
    }

    public void updateScenarioForSession(Long sessionId, Long scenarioId) {
        Optional<GameSession> sessionOpt = gameSessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            session.setCurrentScenarioId(scenarioId);
            session.getVisitedScenarios().add(scenarioId);
            gameSessionRepository.save(session);
            logger.info("Updated scenario for session ID: " + sessionId + " to scenario ID: " + scenarioId);
        } else {
            logger.warning("Game session not found for sessionId: " + sessionId);
        }
    }


    // Recupera gli scenari visitati di una sessione di gioco
    public List<Scenario> getVisitedScenarios(GameSession session) {
        return scenarioRepository.findAllById(session.getVisitedScenarios());
    }

    public boolean checkRiddleAnswer(Long sessionId, Long riddleId, String answer) {
        logger.info("Checking riddle answer for sessionId: " + sessionId + ", riddleId: " + riddleId + ", answer: " + answer);
        Optional<GameSession> sessionOpt = gameSessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            Scenario currentScenario = scenarioRepository.findById(session.getCurrentScenarioId()).orElse(null);
            if (currentScenario != null && currentScenario.getRiddle() != null) {
                Riddle riddle = currentScenario.getRiddle();
                logger.info("Expected answer: " + riddle.getAnswer());
                if (riddle.getId().equals(riddleId) && riddle.getAnswer().equalsIgnoreCase(answer)) {
                    logger.info("Correct riddle answer provided.");
                    // Naviga allo scenario successivo basato sull'ID dell'indovinello
                    Optional<Scenario> nextScenarioOpt = scenarioRepository.findByRiddle_Id(riddle.getId());
                    if (nextScenarioOpt.isPresent()) {
                        Scenario nextScenario = nextScenarioOpt.get();
                        session.setCurrentScenarioId(nextScenario.getId());
                        session.getVisitedScenarios().add(nextScenario.getId());
                        gameSessionRepository.save(session);
                        return true;
                    } else {
                        logger.warning("Next scenario not found for riddleId: " + riddleId);
                    }
                } else {
                    logger.warning("Incorrect riddle answer provided.");
                }
            }
        }
        return false;
    }

    // Recupera l'indovinello tramite ID
    public Riddle getRiddleById(Long riddleId) {
        return scenarioRepository.findByRiddle_Id(riddleId).orElse(null).getRiddle();
    }

    // Recupera la sessione di gioco in corso per un determinato utente e storia
    public Optional<GameSession> getOngoingSession(Long userId, Long storyId) {
        return gameSessionRepository.findByUserIdAndStoryIdAndStatus(userId, storyId, GameSessionStatus.IN_PROGRESS);
    }

    // Termina la sessione di gioco impostando lo stato a COMPLETED
    public void endSession(Long sessionId) {
        Optional<GameSession> sessionOpt = gameSessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            session.setStatus(GameSessionStatus.COMPLETED);
            gameSessionRepository.save(session);
            logger.info("Game session ended and status set to COMPLETED.");
        }
    }
}
