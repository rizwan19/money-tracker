package com.rizwan.money_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExpenseDto implements ExcelExportDto {
    private Long id;
    private String name;
    private LocalDate date;
    private BigDecimal amount;
    private String icon;
    private String categoryName;
    private Long categoryId;
    private LocalDate createdAt;
    private LocalDate modifiedAt;
}
