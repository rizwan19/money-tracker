package com.rizwan.money_tracker.dto.auth;

import com.rizwan.money_tracker.dto.profile.ProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private ProfileDto user;
}
