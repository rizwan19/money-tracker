package com.rizwan.money_tracker.dto.auth;

import com.rizwan.money_tracker.entity.Profile;

public record RefreshTokenRotationResult(
        Profile profile,
        String refreshToken
) {}
