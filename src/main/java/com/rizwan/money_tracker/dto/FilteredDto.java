package com.rizwan.money_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilteredDto {
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String term;
    private String sortField;
    private String order;
}
