package com.healthtracker.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MealRequest {
    private LocalDate date;
    private String name;
    private Integer calories;
}
