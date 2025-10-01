package com.healthtracker.service;

import com.healthtracker.dto.AuthRequest;
import com.healthtracker.dto.AuthResponse;
import com.healthtracker.model.User;
import com.healthtracker.model.Target;
import com.healthtracker.repository.UserRepository;
import com.healthtracker.repository.TargetRepository;
import com.healthtracker.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TargetRepository targetRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername());
        user.setCreatedAt(LocalDateTime.now());
        user.setCurrentStreak(0);
        user.setLongestStreak(0);
        
        user = userRepository.save(user);
        
        Target target = new Target();
        target.setUser(user);
        target.setDailyStepsGoal(10000);
        target.setWeeklyStepsGoal(70000);
        target.setDailyCaloriesGoal(2000);
        target.setWeeklyCaloriesGoal(14000);
        targetRepository.save(target);
        
        String token = jwtUtil.generateToken(user.getUsername());
        
        return new AuthResponse(token, user.getUsername(), user.getDisplayName());
    }
    
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String token = jwtUtil.generateToken(user.getUsername());
        
        return new AuthResponse(token, user.getUsername(), user.getDisplayName());
    }
}
