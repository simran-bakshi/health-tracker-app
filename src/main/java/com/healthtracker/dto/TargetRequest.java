package com.healthtracker.dto;

import lombok.Data;

@Data
public class TargetRequest {
    private Integer dailyStepsGoal;
    private Integer weeklyStepsGoal;
    private Integer dailyCaloriesGoal;
    private Integer weeklyCaloriesGoal;
}
