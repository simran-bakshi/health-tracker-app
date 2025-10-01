package com.healthtracker.service;

import com.healthtracker.model.HealthEntry;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MLService {
    
    public Map<String, Object> predictNextDays(List<HealthEntry> entries, int days) {
        if (entries.isEmpty()) {
            return createEmptyPrediction(days);
        }
        
        SimpleRegression stepsRegression = new SimpleRegression();
        SimpleRegression caloriesRegression = new SimpleRegression();
        
        LocalDate startDate = entries.get(entries.size() - 1).getDate();
        
        for (int i = 0; i < entries.size(); i++) {
            HealthEntry entry = entries.get(entries.size() - 1 - i);
            stepsRegression.addData(i, entry.getSteps());
            caloriesRegression.addData(i, entry.getCalories());
        }
        
        List<Map<String, Object>> predictions = new ArrayList<>();
        LocalDate lastDate = entries.get(0).getDate();
        
        for (int i = 1; i <= days; i++) {
            LocalDate predDate = lastDate.plusDays(i);
            int predSteps = (int) Math.max(0, stepsRegression.predict(entries.size() + i - 1));
            int predCalories = (int) Math.max(0, caloriesRegression.predict(entries.size() + i - 1));
            
            Map<String, Object> pred = new HashMap<>();
            pred.put("date", predDate.toString());
            pred.put("steps", predSteps);
            pred.put("calories", predCalories);
            predictions.add(pred);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("predictions", predictions);
        result.put("confidence", calculateConfidence(stepsRegression));
        return result;
    }
    
    public void detectAnomalies(List<HealthEntry> entries) {
        if (entries.size() < 3) return;
        
        double[] steps = entries.stream().mapToDouble(HealthEntry::getSteps).toArray();
        double[] calories = entries.stream().mapToDouble(HealthEntry::getCalories).toArray();
        
        double stepsMean = Arrays.stream(steps).average().orElse(0);
        double stepsStd = calculateStdDev(steps, stepsMean);
        
        double caloriesMean = Arrays.stream(calories).average().orElse(0);
        double caloriesStd = calculateStdDev(calories, caloriesMean);
        
        for (HealthEntry entry : entries) {
            double stepsZScore = stepsStd > 0 ? Math.abs((entry.getSteps() - stepsMean) / stepsStd) : 0;
            double caloriesZScore = caloriesStd > 0 ? Math.abs((entry.getCalories() - caloriesMean) / caloriesStd) : 0;
            
            if (stepsZScore > 2.5) {
                entry.setIsAnomaly(true);
                entry.setAnomalyType(entry.getSteps() > stepsMean ? "HIGH_STEPS" : "LOW_STEPS");
            } else if (caloriesZScore > 2.5) {
                entry.setIsAnomaly(true);
                entry.setAnomalyType(entry.getCalories() > caloriesMean ? "HIGH_CALORIES" : "LOW_CALORIES");
            } else {
                entry.setIsAnomaly(false);
                entry.setAnomalyType(null);
            }
        }
    }
    
    public List<String> generateAISuggestions(List<HealthEntry> entries, Map<String, Integer> targets) {
        List<String> suggestions = new ArrayList<>();
        
        if (entries.isEmpty()) {
            suggestions.add("Start tracking your daily activities to get personalized recommendations!");
            return suggestions;
        }
        
        double avgSteps = entries.stream().mapToInt(HealthEntry::getSteps).average().orElse(0);
        double avgCalories = entries.stream().mapToInt(HealthEntry::getCalories).average().orElse(0);
        
        int dailyStepsGoal = targets.getOrDefault("dailySteps", 10000);
        int dailyCaloriesGoal = targets.getOrDefault("dailyCalories", 2000);
        
        if (avgSteps < dailyStepsGoal * 0.7) {
            suggestions.add("Your average steps are below target. Try taking a 15-minute walk after meals.");
        } else if (avgSteps > dailyStepsGoal) {
            suggestions.add("Great job! You're exceeding your step goals. Keep up the excellent work!");
        }
        
        if (avgCalories > dailyCaloriesGoal * 1.2) {
            suggestions.add("Consider reducing portion sizes or choosing lower-calorie alternatives.");
        } else if (avgCalories < dailyCaloriesGoal * 0.8) {
            suggestions.add("You might be under-eating. Ensure you're meeting your nutritional needs.");
        }
        
        List<HealthEntry> recent = entries.stream()
            .limit(7)
            .collect(Collectors.toList());
        
        boolean declining = isDecreasingTrend(recent.stream().mapToInt(HealthEntry::getSteps).toArray());
        if (declining) {
            suggestions.add("Your activity has been declining. Set a reminder to move every hour!");
        }
        
        long anomalyCount = entries.stream()
            .limit(14)
            .filter(HealthEntry::getIsAnomaly)
            .count();
        
        if (anomalyCount >= 3) {
            suggestions.add("Detected unusual activity patterns. Try to maintain a consistent routine.");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("You're doing great! Maintain this healthy balance of activity and nutrition.");
        }
        
        return suggestions;
    }
    
    private double calculateStdDev(double[] values, double mean) {
        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        return Math.sqrt(variance);
    }
    
    private double calculateConfidence(SimpleRegression regression) {
        return Math.min(100, Math.max(0, regression.getRSquare() * 100));
    }
    
    private boolean isDecreasingTrend(int[] values) {
        if (values.length < 3) return false;
        int decreasing = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] < values[i - 1]) decreasing++;
        }
        return decreasing > values.length * 0.6;
    }
    
    private Map<String, Object> createEmptyPrediction(int days) {
        List<Map<String, Object>> predictions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 1; i <= days; i++) {
            Map<String, Object> pred = new HashMap<>();
            pred.put("date", today.plusDays(i).toString());
            pred.put("steps", 0);
            pred.put("calories", 0);
            predictions.add(pred);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("predictions", predictions);
        result.put("confidence", 0);
        return result;
    }
}
