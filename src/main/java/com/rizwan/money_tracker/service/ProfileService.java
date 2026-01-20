package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.AuthDto;
import com.rizwan.money_tracker.dto.ProfileDto;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.ProfileRepository;
import com.rizwan.money_tracker.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.util.StringUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
            .map(Profile::isActive)
            .orElse(false);
    }

    public Profile getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with this email " + authentication.getName()));
    }

    public ProfileDto getPublicProfileDto(String email) {
        if (StringUtil.isBlank(email)) {
            return toDto(getCurrentProfile());
        }
        return profileRepository.findByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with this email " + email));
    }

    public Map<String, Object> login(AuthDto authDto) {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword()));
            String token = jwtUtil.generateToken(authDto.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfileDto(authDto.getEmail())
            );
        } catch(Exception e) {
            throw new RuntimeException("Invalid email or password.");
        }
    }

    private Profile toProfile(ProfileDto dto) {
        Profile profile = new Profile();
        profile.setEmail(dto.getEmail());
        profile.setPassword(passwordEncoder.encode(dto.getPassword()));
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
