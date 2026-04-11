package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.IncomeDto;
import com.rizwan.money_tracker.service.ExcelService;
import com.rizwan.money_tracker.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;
    private final ExcelService excelService;

    @PostMapping
    public ResponseEntity<List<IncomeDto>> addIncome(@RequestBody IncomeDto dto) {
        IncomeDto addedIncome = incomeService.addIncome(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(addedIncome));
    }

    @GetMapping
    public ResponseEntity<List<IncomeDto>> getIncomes() {
        List<IncomeDto> incomes = incomeService.getCurrentMonthIncomes();
        return ResponseEntity.ok(incomes);
    }

    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadIncomeExcel() {
        byte[] excelFile = excelService.exportToExcel(incomeService.getCurrentMonthIncomes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income_details.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, String>> emailIncomeDetails() {
        incomeService.emailCurrentMonthIncomeDetails();
        return ResponseEntity.ok(Map.of("message", "Income details emailed successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{incomeId}")
    public ResponseEntity<IncomeDto> updateIncome(@PathVariable Long incomeId, @RequestBody IncomeDto incomeDto) {
        incomeDto.setId(incomeId);
        return ResponseEntity.ok(incomeService.updateIncome(incomeDto));
    }
}
