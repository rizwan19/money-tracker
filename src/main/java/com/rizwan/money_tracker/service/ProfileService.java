package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.ProfileDto;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    public ProfileDto regilsterProfile(ProfileDto dto) {
        Profile newProfile = toProfile(dto);
        newProfile.setToken(UUID.randomUUID().toString());

        String activationLink = "http://localhost:8080/api/v1.0/activate?token=" + newProfile.getToken();
        String subject = "Activate your account";
        String body = "Click on the activation link to activate your account: " + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDto(profileRepository.save(newProfile));
    }

    public boolean activateProfile(String token) {
        return profileRepository.findByToken(token)
            .map(profile -> {
                profile.setActive(true);
                profile.setToken(null);
                profileRepository.save(profile);
                return true;
            })
            .orElse(false);
    }

    private Profile toProfile(ProfileDto dto) {
        Profile profile = new Profile();
        profile.setEmail(dto.getEmail());
        profile.setPassword(dto.getPassword());
        profile.setProfileImageUrl(dto.getProfileImageUrl());
        profile.setFullName(dto.getFullName());
        return profile;
    }

    private ProfileDto toDto(Profile profile) {
        return ProfileDto.builder()
                .email(profile.getEmail())
                .fullName(profile.getFullName())
                .profileImageUrl(profile.getProfileImageUrl())
                .token(profile.getToken())
                .active(profile.isActive())
                .build();
    }
}
