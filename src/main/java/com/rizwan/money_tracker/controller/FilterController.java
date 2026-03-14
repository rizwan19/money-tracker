package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.ExpenseDto;
import com.rizwan.money_tracker.dto.FilteredDto;
import com.rizwan.money_tracker.dto.IncomeDto;
import com.rizwan.money_tracker.service.ExpenseService;
import com.rizwan.money_tracker.service.IncomeService;
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

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilteredDto dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        String term = dto.getTerm() != null ? dto.getTerm() : "";
        String sortField = resolveSortField(dto.getSortField());
        Sort.Direction direction = "desc".equalsIgnoreCase(dto.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        if ("income".equalsIgnoreCase(dto.getType())) {
            List<IncomeDto> incomes = incomeService.filterIncomes(startDate, endDate, term, sort);
            return ResponseEntity.ok(incomes);
        }
        else if ("expense".equalsIgnoreCase(dto.getType())) {
            List<ExpenseDto> expenses = expenseService.filterExpenses(startDate, endDate, term, sort);
            return ResponseEntity.ok(expenses);
        }
        else {
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
