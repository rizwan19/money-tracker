package com.rizwan.money_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExcelExportDto {

    Long getId();

    String getName();

    String getCategoryName();

    BigDecimal getAmount();

    LocalDate getDate();

    LocalDate getCreatedAt();

    LocalDate getModifiedAt();
}
