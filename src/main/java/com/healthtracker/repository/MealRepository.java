package com.healthtracker.repository;

import com.healthtracker.model.Meal;
import com.healthtracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByUserAndDate(User user, LocalDate date);
    List<Meal> findByUserAndDateBetweenOrderByDateDescTimeDesc(User user, LocalDate start, LocalDate end);
}
