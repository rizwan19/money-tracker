package com.rizwan.money_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProfileDto {
    private String fullName;
    private String email;
    private String password;
    private String profileImageUrl;
    private Boolean active;
    private String token;
}
