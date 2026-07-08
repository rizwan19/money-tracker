package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.AuthDto;
import com.rizwan.money_tracker.dto.auth.AuthTokensResult;
import com.rizwan.money_tracker.dto.auth.LoginResponseDto;
import com.rizwan.money_tracker.dto.profile.ProfileDto;
import com.rizwan.money_tracker.dto.profile.ProfileUpdateRequest;
import com.rizwan.money_tracker.entity.RefreshToken;
import com.rizwan.money_tracker.service.ProfileService;
import com.rizwan.money_tracker.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_COOKIE_PATH = "/api/v1.0/profile";

    private final ProfileService profileService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.refresh-cookie.secure:true}")
    private boolean refreshCookieSecure;

    @Value("${app.refresh-cookie.same-site:None}")
    private String refreshCookieSameSite;

    @PostMapping("/register")
    public ResponseEntity<ProfileDto> registerProfile(@RequestBody ProfileDto dto) {
        ProfileDto registeredProfile = profileService.registerProfile(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<Map<String, Object>> activateProfile(@RequestParam String token) {
        boolean activated = profileService.activateProfile(token);
        if (activated) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile activated successfully."
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Invalid activation token."
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto authDto) {
        try {
            if (!profileService.isAccountPresent(authDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Account not found. Please register first."));
            }
            if (!profileService.isAccountActive(authDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Account is not activated"));
            }
            AuthTokensResult tokens = profileService.login(authDto);
            ResponseCookie cookie = buildRefreshCookie(tokens.getRefreshToken(), Duration.ofDays(7));
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(tokens.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDto> refreshToken(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        RefreshToken stored = refreshTokenService.validate(refreshToken);
        LoginResponseDto response = profileService.issueAccessToken(stored.getProfile());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        refreshTokenService.revoke(refreshToken);
        ResponseCookie expired = buildRefreshCookie("", Duration.ZERO);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .build();
    }

    @GetMapping("/test")
    public String test() {
        return "Hello World";
    }

    @GetMapping("/info")
    public ResponseEntity<ProfileDto> getPublicProfile() {
        return ResponseEntity.ok(profileService.getPublicProfile(null));
    }

    @PutMapping("/update")
    public ResponseEntity<ProfileDto> updateProfile(@RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @GetMapping("/details")
    public ResponseEntity<ProfileDto> getProfileDetails() {
        return ResponseEntity.ok(profileService.getProfileDetails());
    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path(REFRESH_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
