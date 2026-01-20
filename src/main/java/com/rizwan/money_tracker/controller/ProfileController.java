package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.AuthDto;
import com.rizwan.money_tracker.dto.ProfileDto;
import com.rizwan.money_tracker.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDto> registerProfile(@RequestBody ProfileDto dto) {
        ProfileDto registeredProfile = profileService.regilsterProfile(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        boolean activated = profileService.activateProfile(token);
        if (activated) {
            return ResponseEntity.ok("Profile activated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid activation token.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDto authDto) {
        try{
            if(!profileService.isAccountActive(authDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Account is not activated. Please check your email for the activation link."));
            }
            Map<String, Object> response = profileService.login(authDto);
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/test")
    public String test() {
        return "Hello World";
    }
}
