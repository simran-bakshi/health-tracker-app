package com.healthtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "health_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;
    
    private Integer steps = 0;
    
    private Integer calories = 0;
    
    private Boolean isAnomaly = false;
    
    private String anomalyType;
}
