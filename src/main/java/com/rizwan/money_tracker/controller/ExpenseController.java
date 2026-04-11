package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.ExpenseDto;
import com.rizwan.money_tracker.service.ExcelService;
import com.rizwan.money_tracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExcelService excelService;

    @PostMapping
    public ResponseEntity<List<ExpenseDto>> addExpense(@RequestBody ExpenseDto dto) {
        ExpenseDto createdExpense = expenseService.addExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(createdExpense));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpenses() {
        List<ExpenseDto> expenses = expenseService.getCurrentMonthExpenses();
        return ResponseEntity.ok(expenses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadExpenseExcel() {
        byte[] excelFile = excelService.exportToExcel(expenseService.getCurrentMonthExpenses());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expense_details.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, String>> emailExpenseDetails() {
        expenseService.emailCurrentMonthExpenseDetails();
        return ResponseEntity.ok(Map.of("message", "Expense details emailed successfully"));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDto> updateIncome(@PathVariable Long expenseId, @RequestBody ExpenseDto expenseDto) {
        expenseDto.setId(expenseId);
        return ResponseEntity.ok(expenseService.updateExpense(expenseDto));
    }
}
