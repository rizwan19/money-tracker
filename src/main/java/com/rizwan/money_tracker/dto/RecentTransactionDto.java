package com.rizwan.money_tracker.dto;

import com.rizwan.money_tracker.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private LocalDate date;
    private LocalDate createdAt;
    private LocalDate modifiedAt;
    private TransactionType type;
}
