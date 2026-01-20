package com.rizwan.money_tracker.repository;

import com.rizwan.money_tracker.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository <Profile, Long> {
    Optional<Profile> findByEmail(String email);
    Optional<Profile> findByToken(String token);
}
