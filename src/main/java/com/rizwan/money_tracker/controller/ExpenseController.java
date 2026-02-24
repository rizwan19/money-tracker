package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.ExpenseDto;
import com.rizwan.money_tracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<List<ExpenseDto>> addExpense(@RequestBody ExpenseDto dto) {
        ExpenseDto createdExpense = expenseService.addExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(createdExpense));
    }
}
