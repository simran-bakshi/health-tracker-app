package com.healthtracker.repository;

import com.healthtracker.model.Friend;
import com.healthtracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUser(User user);
    Optional<Friend> findByUserAndFriend(User user, User friend);
}
