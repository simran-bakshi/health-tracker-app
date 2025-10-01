package com.healthtracker.repository;

import com.healthtracker.model.HealthEntry;
import com.healthtracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthEntryRepository extends JpaRepository<HealthEntry, Long> {
    List<HealthEntry> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);
    List<HealthEntry> findByUserOrderByDateDesc(User user);
    Optional<HealthEntry> findByUserAndDate(User user, LocalDate date);
}
