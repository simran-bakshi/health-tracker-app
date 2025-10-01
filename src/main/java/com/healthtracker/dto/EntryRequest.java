package com.healthtracker.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EntryRequest {
    private LocalDate date;
    private Integer steps;
    private Integer calories;
}
