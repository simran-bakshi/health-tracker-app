package com.healthtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "targets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Target {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;
    
    private Integer dailyStepsGoal = 10000;
    
    private Integer weeklyStepsGoal = 70000;
    
    private Integer dailyCaloriesGoal = 2000;
    
    private Integer weeklyCaloriesGoal = 14000;
}
