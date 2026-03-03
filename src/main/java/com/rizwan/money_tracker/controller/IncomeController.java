package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.IncomeDto;
import com.rizwan.money_tracker.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<List<IncomeDto>> addIncome(@RequestBody IncomeDto dto) {
        IncomeDto addedIncome = incomeService.addIncome(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(addedIncome));
    }

    @GetMapping
    public ResponseEntity<List<IncomeDto>> getExpenses() {
        List<IncomeDto> incomes = incomeService.getCurrentMonthIncomes();
        return ResponseEntity.ok(incomes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }
}
