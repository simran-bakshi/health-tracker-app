package com.healthtracker.service;

import com.healthtracker.dto.EntryRequest;
import com.healthtracker.dto.MealRequest;
import com.healthtracker.model.*;
import com.healthtracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HealthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private HealthEntryRepository healthEntryRepository;
    
    @Autowired
    private MealRepository mealRepository;
    
    @Autowired
    private FriendRepository friendRepository;
    
    @Autowired
    private TargetRepository targetRepository;
    
    @Autowired
    private MLService mlService;
    
    public List<HealthEntry> getEntries(String username, int days) {
        User user = getUser(username);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        List<HealthEntry> entries = healthEntryRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
        mlService.detectAnomalies(entries);
        healthEntryRepository.saveAll(entries);
        
        return entries;
    }
    
    public HealthEntry getEntry(String username, String dateStr) {
        User user = getUser(username);
        LocalDate date = LocalDate.parse(dateStr);
        
        return healthEntryRepository.findByUserAndDate(user, date)
                .orElse(null);
    }
    
    public HealthEntry saveEntry(String username, EntryRequest request) {
        User user = getUser(username);
        
        HealthEntry entry = healthEntryRepository.findByUserAndDate(user, request.getDate())
                .orElse(new HealthEntry());
        
        entry.setUser(user);
        entry.setDate(request.getDate());
        
        if (request.getSteps() != null) {
            entry.setSteps(request.getSteps());
        }
        
        if (request.getCalories() != null) {
            entry.setCalories(entry.getCalories() + request.getCalories());
        }
        
        entry = healthEntryRepository.save(entry);
        
        updateStreak(user);
        
        return entry;
    }
    
    public Meal saveMeal(String username, MealRequest request) {
        User user = getUser(username);
        
        Meal meal = new Meal();
        meal.setUser(user);
        meal.setDate(request.getDate());
        meal.setTime(LocalTime.now());
        meal.setName(request.getName());
        meal.setCalories(request.getCalories());
        
        meal = mealRepository.save(meal);
        
        HealthEntry entry = healthEntryRepository.findByUserAndDate(user, request.getDate())
                .orElse(new HealthEntry());
        entry.setUser(user);
        entry.setDate(request.getDate());
        entry.setCalories(entry.getCalories() + request.getCalories());
        healthEntryRepository.save(entry);
        
        return meal;
    }
    
    public Map<String, Object> getSummary(String username) {
        User user = getUser(username);
        Target target = targetRepository.findByUser(user)
                .orElseGet(() -> createDefaultTarget(user));
        
        List<HealthEntry> entries = getEntries(username, 14);
        
        LocalDate today = LocalDate.now();
        HealthEntry todayEntry = healthEntryRepository.findByUserAndDate(user, today)
                .orElse(new HealthEntry(null, user, today, 0, 0, false, null));
        
        List<Meal> todayMeals = mealRepository.findByUserAndDate(user, today);
        
        Map<String, Integer> targets = new HashMap<>();
        targets.put("dailySteps", target.getDailyStepsGoal());
        targets.put("dailyCalories", target.getDailyCaloriesGoal());
        
        List<String> suggestions = mlService.generateAISuggestions(entries, targets);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("todaySteps", todayEntry.getSteps());
        summary.put("todayCalories", todayEntry.getCalories());
        summary.put("dailyStepsGoal", target.getDailyStepsGoal());
        summary.put("dailyCaloriesGoal", target.getDailyCaloriesGoal());
        summary.put("stepsProgress", calculateProgress(todayEntry.getSteps(), target.getDailyStepsGoal()));
        summary.put("caloriesProgress", calculateProgress(todayEntry.getCalories(), target.getDailyCaloriesGoal()));
        summary.put("currentStreak", user.getCurrentStreak());
        summary.put("longestStreak", user.getLongestStreak());
        summary.put("todayMeals", todayMeals);
        summary.put("aiSuggestions", suggestions);
        
        return summary;
    }
    
    public Map<String, Object> predict(String username, int days) {
        User user = getUser(username);
        List<HealthEntry> entries = healthEntryRepository.findByUserOrderByDateDesc(user);
        
        if (entries.size() > 30) {
            entries = entries.subList(0, 30);
        }
        
        Collections.reverse(entries);
        return mlService.predictNextDays(entries, days);
    }
    
    public List<Map<String, Object>> getLeaderboard(String username) {
        User currentUser = getUser(username);
        List<Friend> friends = friendRepository.findByUser(currentUser);
        
        List<User> users = new ArrayList<>();
        users.add(currentUser);
        users.addAll(friends.stream().map(Friend::getFriend).collect(Collectors.toList()));
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        
        for (User user : users) {
            List<HealthEntry> entries = healthEntryRepository.findByUserAndDateBetweenOrderByDateDesc(user, weekStart, today);
            
            int totalSteps = entries.stream().mapToInt(HealthEntry::getSteps).sum();
            int totalCalories = entries.stream().mapToInt(HealthEntry::getCalories).sum();
            
            HealthEntry todayEntry = healthEntryRepository.findByUserAndDate(user, today).orElse(null);
            int todaySteps = todayEntry != null ? todayEntry.getSteps() : 0;
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("displayName", user.getDisplayName());
            userData.put("weeklySteps", totalSteps);
            userData.put("weeklyCalories", totalCalories);
            userData.put("todaySteps", todaySteps);
            userData.put("currentStreak", user.getCurrentStreak());
            userData.put("isCurrentUser", user.getId().equals(currentUser.getId()));
            
            leaderboard.add(userData);
        }
        
        leaderboard.sort((a, b) -> Integer.compare((Integer) b.get("weeklySteps"), (Integer) a.get("weeklySteps")));
        
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).put("rank", i + 1);
        }
        
        return leaderboard;
    }
    
    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }
    
    public Friend addFriend(String username, String friendUsername) {
        User user = getUser(username);
        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getId().equals(friend.getId())) {
            throw new RuntimeException("Cannot add yourself as friend");
        }
        
        if (friendRepository.findByUserAndFriend(user, friend).isPresent()) {
            throw new RuntimeException("Already friends");
        }
        
        Friend friendship = new Friend();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setCreatedAt(java.time.LocalDateTime.now());
        
        return friendRepository.save(friendship);
    }
    
    public List<Map<String, Object>> getFriendsActivity(String username) {
        User user = getUser(username);
        List<Friend> friends = friendRepository.findByUser(user);
        
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> activities = new ArrayList<>();
        
        for (Friend friendship : friends) {
            User friend = friendship.getFriend();
            HealthEntry todayEntry = healthEntryRepository.findByUserAndDate(friend, today).orElse(null);
            
            if (todayEntry != null && (todayEntry.getSteps() > 0 || todayEntry.getCalories() > 0)) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("username", friend.getUsername());
                activity.put("displayName", friend.getDisplayName());
                activity.put("steps", todayEntry.getSteps());
                activity.put("calories", todayEntry.getCalories());
                activity.put("date", today.toString());
                activities.add(activity);
            }
        }
        
        return activities;
    }

    public Target updateTargets(String username, Map<String, Integer> targetsMap) {
        User user = getUser(username);
        
        // Find existing target or create new one WITHOUT saving it yet
        Target target = targetRepository.findByUser(user)
                .orElseGet(() -> {
                    Target newTarget = new Target();
                    newTarget.setUser(user);
                    newTarget.setDailyStepsGoal(10000);
                    newTarget.setWeeklyStepsGoal(70000);
                    newTarget.setDailyCaloriesGoal(2000);
                    newTarget.setWeeklyCaloriesGoal(14000);
                    return newTarget;
                });
        
        // Update the target values
        if (targetsMap.containsKey("dailyStepsGoal")) {
            target.setDailyStepsGoal(targetsMap.get("dailyStepsGoal"));
        }
        if (targetsMap.containsKey("weeklyStepsGoal")) {
            target.setWeeklyStepsGoal(targetsMap.get("weeklyStepsGoal"));
        }
        if (targetsMap.containsKey("dailyCaloriesGoal")) {
            target.setDailyCaloriesGoal(targetsMap.get("dailyCaloriesGoal"));
        }
        if (targetsMap.containsKey("weeklyCaloriesGoal")) {
            target.setWeeklyCaloriesGoal(targetsMap.get("weeklyCaloriesGoal"));
        }
        
        // Save only once here
        return targetRepository.save(target);
    }
    
    public Map<String, Object> getMonthlyReport(String username, int year, int month) {
        User user = getUser(username);
        Target target = targetRepository.findByUser(user)
                .orElseGet(() -> createDefaultTarget(user));
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<HealthEntry> entries = healthEntryRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
        Collections.reverse(entries);
        
        int totalSteps = entries.stream().mapToInt(HealthEntry::getSteps).sum();
        int totalCalories = entries.stream().mapToInt(HealthEntry::getCalories).sum();
        int avgSteps = entries.isEmpty() ? 0 : totalSteps / entries.size();
        int avgCalories = entries.isEmpty() ? 0 : totalCalories / entries.size();
        
        int maxSteps = entries.stream().mapToInt(HealthEntry::getSteps).max().orElse(0);
        int minSteps = entries.stream().mapToInt(HealthEntry::getSteps).min().orElse(0);
        
        long anomalies = entries.stream().filter(HealthEntry::getIsAnomaly).count();
        
        Map<String, Integer> targets = new HashMap<>();
        targets.put("dailySteps", target.getDailyStepsGoal());
        targets.put("dailyCalories", target.getDailyCaloriesGoal());
        
        List<String> suggestions = mlService.generateAISuggestions(entries, targets);
        
        Map<String, Object> report = new HashMap<>();
        report.put("month", month);
        report.put("year", year);
        report.put("totalSteps", totalSteps);
        report.put("totalCalories", totalCalories);
        report.put("avgSteps", avgSteps);
        report.put("avgCalories", avgCalories);
        report.put("maxSteps", maxSteps);
        report.put("minSteps", minSteps);
        report.put("activeDays", entries.size());
        report.put("anomalies", anomalies);
        report.put("entries", entries);
        report.put("aiSuggestions", suggestions);
        report.put("currentStreak", user.getCurrentStreak());
        
        return report;
    }
    
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private Target createDefaultTarget(User user) {
        // Check if target already exists to prevent duplicates
        Optional<Target> existing = targetRepository.findByUser(user);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        Target target = new Target();
        target.setUser(user);
        target.setDailyStepsGoal(10000);
        target.setWeeklyStepsGoal(70000);
        target.setDailyCaloriesGoal(2000);
        target.setWeeklyCaloriesGoal(14000);
        return targetRepository.save(target);
    }
    
    private int calculateProgress(int current, int goal) {
        if (goal == 0) return 0;
        return Math.min(100, (current * 100) / goal);
    }
    
    private void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        HealthEntry todayEntry = healthEntryRepository.findByUserAndDate(user, today).orElse(null);
        
        if (todayEntry != null && todayEntry.getSteps() > 0) {
            if (user.getLastActivityDate() == null) {
                user.setCurrentStreak(1);
            } else if (user.getLastActivityDate().toLocalDate().equals(yesterday)) {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
            } else if (!user.getLastActivityDate().toLocalDate().equals(today)) {
                user.setCurrentStreak(1);
            }
            
            if (user.getCurrentStreak() > user.getLongestStreak()) {
                user.setLongestStreak(user.getCurrentStreak());
            }
            
            user.setLastActivityDate(java.time.LocalDateTime.now());
            userRepository.save(user);
        }
    }
}