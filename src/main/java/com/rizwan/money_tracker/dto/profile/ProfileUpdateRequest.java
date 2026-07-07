package com.rizwan.money_tracker.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProfileUpdateRequest {
    private Long id;
    private String fullName;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
    private String profileImageUrl;
}
