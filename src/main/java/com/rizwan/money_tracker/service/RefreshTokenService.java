package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.entity.RefreshToken;
import com.rizwan.money_tracker.exception.InvalidRefreshTokenException;
import com.rizwan.money_tracker.repository.RefreshTokenRepository;
import com.rizwan.money_tracker.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    /**
     * Revoke any existing tokens for the profile and issue a new raw refresh token.
     * Only the SHA-256 hash is persisted; the raw token is returned to the caller.
     */
    @Transactional
    public String createRefreshToken(Profile profile) {
        refreshTokenRepository.deleteByProfileId(profile.getId());

        String rawToken = jwtUtil.generateRefreshToken(profile.getEmail());

        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(hash(rawToken));
        entity.setProfile(profile);
        entity.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);

        return rawToken;
    }

    /**
     * Validate that the raw refresh token maps to a stored, non-revoked, unexpired record.
     * Throws {@link InvalidRefreshTokenException} (401) otherwise.
     */
    public RefreshToken validate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("Missing refresh token.");
        }
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token."));
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired.");
        }
        return stored;
    }

    /**
     * Revoke the token matching the raw value if present. No-op when absent.
     */
    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(rawToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
