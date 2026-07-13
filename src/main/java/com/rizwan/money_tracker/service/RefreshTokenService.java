package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.auth.RefreshTokenRotationResult;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.exception.InvalidRefreshTokenException;
import com.rizwan.money_tracker.exception.RefreshTokenStoreUnavailableException;
import com.rizwan.money_tracker.repository.ProfileRepository;
import com.rizwan.money_tracker.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    /**
     * Atomic compare-and-set: replace the stored hash (and reset TTL) only if the presented
     * hash is still current. Prevents two concurrent refreshes with the same cookie from both
     * succeeding.
     */
    private static final RedisScript<Long> COMPARE_AND_SET = new DefaultRedisScript<>(
            """
                    if redis.call('GET', KEYS[1]) == ARGV[1] then
                        redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3])
                        return 1
                    end
                    return 0""",
            Long.class);

    /**
     * Atomic compare-and-delete: delete the key only if it still holds the presented hash.
     * Prevents a stale logout from deleting a newer session created by login or rotation.
     */
    private static final RedisScript<Long> COMPARE_AND_DELETE = new DefaultRedisScript<>(
            """
                    if redis.call('GET', KEYS[1]) == ARGV[1] then
                        return redis.call('DEL', KEYS[1])
                    end
                    return 0""",
            Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ProfileRepository profileRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Value("${app.refresh-token.redis-key-prefix:money-tracker:refresh:profile}")
    private String redisKeyPrefix;

    /**
     * Issue a new raw refresh token for the profile and store only its SHA-256 hash in Redis
     * under the profile key with a millisecond TTL. The single {@code SET} atomically replaces
     * any previously active session for the profile.
     */
    public String createRefreshToken(Profile profile) {
        if (profile == null || profile.getId() == null) {
            throw new IllegalArgumentException("A persisted profile with a non-null id is required.");
        }
        String rawToken = jwtUtil.generateRefreshToken(profile.getEmail());
        String key = keyFor(profile.getId());
        try {
            redisTemplate.opsForValue().set(key, hash(rawToken), Duration.ofMillis(refreshExpirationMs));
        } catch (DataAccessException e) {
            throw storeUnavailable("createRefreshToken", e);
        }
        return rawToken;
    }

    /**
     * Verify and rotate the presented refresh token. On success the Redis value is atomically
     * replaced with the hash of a freshly issued token and its TTL reset.
     * Throws {@link InvalidRefreshTokenException} (401) for missing/invalid/replayed tokens and
     * {@link RefreshTokenStoreUnavailableException} (503) when Redis is unreachable.
     */
    public RefreshTokenRotationResult rotateRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("Missing refresh token.");
        }
        String email = jwtUtil.parseRefreshToken(rawToken);
        Profile profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token."));

        String presentedHash = hash(rawToken);
        String replacementRaw = jwtUtil.generateRefreshToken(profile.getEmail());
        String replacementHash = hash(replacementRaw);
        String key = keyFor(profile.getId());

        Long result;
        try {
            result = redisTemplate.execute(
                    COMPARE_AND_SET,
                    List.of(key),
                    presentedHash,
                    replacementHash,
                    Long.toString(refreshExpirationMs));
        } catch (DataAccessException e) {
            throw storeUnavailable("rotateRefreshToken", e);
        }

        if (result == null || result == 0L) {
            throw new InvalidRefreshTokenException("Invalid refresh token.");
        }
        return new RefreshTokenRotationResult(profile, replacementRaw);
    }

    /**
     * Revoke the presented token. No-op for missing, malformed, or expired tokens. Uses a
     * compare-and-delete so a stale logout cannot delete a newer session.
     * Throws {@link RefreshTokenStoreUnavailableException} (503) when Redis is unreachable.
     */
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        String email;
        try {
            email = jwtUtil.parseRefreshToken(rawToken);
        } catch (InvalidRefreshTokenException e) {
            return;
        }
        Profile profile = profileRepository.findByEmail(email).orElse(null);
        if (profile == null || profile.getId() == null) {
            return;
        }
        String key = keyFor(profile.getId());
        try {
            redisTemplate.execute(COMPARE_AND_DELETE, List.of(key), hash(rawToken));
        } catch (DataAccessException e) {
            throw storeUnavailable("revoke", e);
        }
    }

    private String keyFor(Long profileId) {
        return redisKeyPrefix + ":" + profileId;
    }

    private RefreshTokenStoreUnavailableException storeUnavailable(String operation, DataAccessException e) {
        log.error("Refresh-token store unavailable during {}: {}", operation, e.getClass().getSimpleName(), e);
        return new RefreshTokenStoreUnavailableException("Refresh-token store is unavailable.", e);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
