package com.healthtracker.controller;

import com.healthtracker.dto.EntryRequest;
import com.healthtracker.dto.MealRequest;
import com.healthtracker.model.*;
import com.healthtracker.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {
    
    @Autowired
    private HealthService healthService;
    
    @GetMapping("/entries")
    public ResponseEntity<?> getEntries(@RequestParam(defaultValue = "14") int days, Authentication auth) {
        try {
            String username = auth.getName();
            List<HealthEntry> entries = healthService.getEntries(username, days);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @GetMapping("/entry/{date}")
    public ResponseEntity<?> getEntry(@PathVariable String date, Authentication auth) {
        try {
            String username = auth.getName();
            HealthEntry entry = healthService.getEntry(username, date);
            if (entry == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @PostMapping("/entry")
    public ResponseEntity<?> saveEntry(@RequestBody EntryRequest request, Authentication auth) {
        try {
            String username = auth.getName();
            if (request.getDate() == null) {
                request.setDate(LocalDate.now());
            }
            HealthEntry entry = healthService.saveEntry(username, request);
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @PostMapping("/meal")
    public ResponseEntity<?> saveMeal(@RequestBody MealRequest request, Authentication auth) {
        try {
            String username = auth.getName();
            if (request.getDate() == null) {
                request.setDate(LocalDate.now());
            }
            Meal meal = healthService.saveMeal(username, request);
            return ResponseEntity.ok(meal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(Authentication auth) {
        try {
            String username = auth.getName();
            Map<String, Object> summary = healthService.getSummary(username);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @GetMapping("/predict")
    public ResponseEntity<?> predict(@RequestParam(defaultValue = "3") int days, Authentication auth) {
        try {
            String username = auth.getName();
            Map<String, Object> predictions = healthService.predict(username, days);
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(Authentication auth) {
        try {
            String username = auth.getName();
            List<Map<String, Object>> leaderboard = healthService.getLeaderboard(username);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q, Authentication auth) {
        try {
            List<User> users = healthService.searchUsers(q);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @PostMapping("/friends")
    public ResponseEntity<?> addFriend(@RequestBody Map<String, String> request, Authentication auth) {
        try {
            String username = auth.getName();
            String friendUsername = request.get("friendUsername");
            Friend friend = healthService.addFriend(username, friendUsername);
            return ResponseEntity.ok(friend);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @GetMapping("/friends-activity")
    public ResponseEntity<?> getFriendsActivity(Authentication auth) {
        try {
            String username = auth.getName();
            List<Map<String, Object>> activities = healthService.getFriendsActivity(username);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @PutMapping("/targets")
    public ResponseEntity<?> updateTargets(@RequestBody Map<String, Integer> targets, Authentication auth) {
        try {
            String username = auth.getName();
            Target target = healthService.updateTargets(username, targets);
            return ResponseEntity.ok(target);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    @GetMapping("/monthly-report")
    public ResponseEntity<?> getMonthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication auth) {
        try {
            String username = auth.getName();
            LocalDate now = LocalDate.now();
            int y = year != null ? year : now.getYear();
            int m = month != null ? month : now.getMonthValue();
            
            Map<String, Object> report = healthService.getMonthlyReport(username, y, m);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
    
    private Map<String, String> createError(String message) {
        Map<String, String> error = new java.util.HashMap<>();
        error.put("error", message);
        return error;
    }
}
