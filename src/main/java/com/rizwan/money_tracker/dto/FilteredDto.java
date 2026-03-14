package com.rizwan.money_tracker.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilteredDto {
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    @JsonAlias("keyword")
    private String term;
    private String sortField;
    @JsonAlias("sortOrder")
    private String order;
}
