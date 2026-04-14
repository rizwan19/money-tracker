package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.FilteredDto;
import com.rizwan.money_tracker.dto.TransactionDto;
import com.rizwan.money_tracker.entity.TransactionType;
import com.rizwan.money_tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/filter")
@RequiredArgsConstructor
public class FilterController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilteredDto dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        String term = dto.getTerm() != null ? dto.getTerm() : "";
        String sortField = resolveSortField(dto.getSortField());
        Sort.Direction direction = "desc".equalsIgnoreCase(dto.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        try {
            TransactionType transactionType = TransactionType.fromValue(dto.getType());
            List<TransactionDto> transactions = transactionService.filterTransactions(transactionType, startDate, endDate, term, sort);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'.");
        }
    }

    private String resolveSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return "date";
        }

        return switch (sortField.toLowerCase(Locale.ROOT)) {
            case "amount" -> "amount";
            case "date" -> "date";
            case "name" -> "name";
            case "createdat" -> "createdAt";
            case "modifiedat" -> "modifiedAt";
            case "category" -> "category.name";
            default -> "date";
        };
    }
}
