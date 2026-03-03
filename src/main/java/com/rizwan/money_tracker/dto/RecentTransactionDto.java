package com.rizwan.money_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentTransactionDto {
    private Long id;
    private Long profileId;
    private String name;
    private String icon;
    private BigDecimal amount;
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String type;
}
