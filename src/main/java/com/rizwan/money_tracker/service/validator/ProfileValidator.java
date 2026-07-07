package com.rizwan.money_tracker.service.validator;

import com.rizwan.money_tracker.dto.profile.ProfileUpdateRequest;
import com.rizwan.money_tracker.entity.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileValidator {
    private final PasswordEncoder passwordEncoder;

    public boolean hasNewPassword(ProfileUpdateRequest request) {
        return request.getNewPassword()!=null && !request.getNewPassword().isBlank();
    }
    public void validatePasswordUpdate(Profile profile, ProfileUpdateRequest request) {
        if (request.getCurrentPassword()==null || request.getCurrentPassword().isBlank())
            throw new RuntimeException("Current password is required");
        if (!passwordEncoder.matches(request.getCurrentPassword(), profile.getPassword()))
            throw new RuntimeException("Current password is incorrect");
        if (request.getConfirmPassword()==null || request.getConfirmPassword().isBlank())
            throw new RuntimeException("Confirm password is empty");
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new RuntimeException("New passowrd and confirm password dont match");
    }
}
