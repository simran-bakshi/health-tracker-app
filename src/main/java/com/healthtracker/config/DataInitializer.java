package com.healthtracker.config;

import com.healthtracker.model.*;
import com.healthtracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {
    
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
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            initializeSampleData();
        }
    }
    
    private void initializeSampleData() {
        Random random = new Random();
        
        User user1 = new User();
        user1.setUsername("john_doe");
        user1.setEmail("john@example.com");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setDisplayName("John Doe");
        user1.setCreatedAt(LocalDateTime.now().minusDays(30));
        user1.setCurrentStreak(7);
        user1.setLongestStreak(14);
        user1.setLastActivityDate(LocalDateTime.now());
        user1 = userRepository.save(user1);
        
        User user2 = new User();
        user2.setUsername("jane_smith");
        user2.setEmail("jane@example.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setDisplayName("Jane Smith");
        user2.setCreatedAt(LocalDateTime.now().minusDays(25));
        user2.setCurrentStreak(5);
        user2.setLongestStreak(10);
        user2.setLastActivityDate(LocalDateTime.now());
        user2 = userRepository.save(user2);
        
        User user3 = new User();
        user3.setUsername("bob_wilson");
        user3.setEmail("bob@example.com");
        user3.setPassword(passwordEncoder.encode("password123"));
        user3.setDisplayName("Bob Wilson");
        user3.setCreatedAt(LocalDateTime.now().minusDays(20));
        user3.setCurrentStreak(3);
        user3.setLongestStreak(8);
        user3.setLastActivityDate(LocalDateTime.now());
        user3 = userRepository.save(user3);
        
        Target target1 = new Target();
        target1.setUser(user1);
        target1.setDailyStepsGoal(10000);
        target1.setWeeklyStepsGoal(70000);
        target1.setDailyCaloriesGoal(2000);
        target1.setWeeklyCaloriesGoal(14000);
        targetRepository.save(target1);
        
        Target target2 = new Target();
        target2.setUser(user2);
        target2.setDailyStepsGoal(8000);
        target2.setWeeklyStepsGoal(56000);
        target2.setDailyCaloriesGoal(1800);
        target2.setWeeklyCaloriesGoal(12600);
        targetRepository.save(target2);
        
        Target target3 = new Target();
        target3.setUser(user3);
        target3.setDailyStepsGoal(12000);
        target3.setWeeklyStepsGoal(84000);
        target3.setDailyCaloriesGoal(2200);
        target3.setWeeklyCaloriesGoal(15400);
        targetRepository.save(target3);
        
        for (int i = 0; i < 30; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            
            HealthEntry entry1 = new HealthEntry();
            entry1.setUser(user1);
            entry1.setDate(date);
            entry1.setSteps(8000 + random.nextInt(5000));
            entry1.setCalories(1800 + random.nextInt(600));
            healthEntryRepository.save(entry1);
            
            HealthEntry entry2 = new HealthEntry();
            entry2.setUser(user2);
            entry2.setDate(date);
            entry2.setSteps(6000 + random.nextInt(4000));
            entry2.setCalories(1500 + random.nextInt(500));
            healthEntryRepository.save(entry2);
            
            HealthEntry entry3 = new HealthEntry();
            entry3.setUser(user3);
            entry3.setDate(date);
            entry3.setSteps(10000 + random.nextInt(6000));
            entry3.setCalories(2000 + random.nextInt(800));
            healthEntryRepository.save(entry3);
        }
        
        String[] mealNames = {"Breakfast", "Lunch", "Dinner", "Snack"};
        int[] mealCalories = {400, 600, 700, 200};
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            
            for (int j = 0; j < mealNames.length; j++) {
                Meal meal1 = new Meal();
                meal1.setUser(user1);
                meal1.setDate(date);
                meal1.setTime(LocalTime.of(7 + j * 4, 0));
                meal1.setName(mealNames[j]);
                meal1.setCalories(mealCalories[j] + random.nextInt(100));
                mealRepository.save(meal1);
            }
        }
        
        Friend friend1 = new Friend();
        friend1.setUser(user1);
        friend1.setFriend(user2);
        friend1.setCreatedAt(LocalDateTime.now().minusDays(10));
        friendRepository.save(friend1);
        
        Friend friend2 = new Friend();
        friend2.setUser(user1);
        friend2.setFriend(user3);
        friend2.setCreatedAt(LocalDateTime.now().minusDays(8));
        friendRepository.save(friend2);
        
        Friend friend3 = new Friend();
        friend3.setUser(user2);
        friend3.setFriend(user1);
        friend3.setCreatedAt(LocalDateTime.now().minusDays(10));
        friendRepository.save(friend3);
        
        System.out.println("Sample data initialized successfully!");
        System.out.println("Test users:");
        System.out.println("  - john_doe / password123");
        System.out.println("  - jane_smith / password123");
        System.out.println("  - bob_wilson / password123");
    }
}
