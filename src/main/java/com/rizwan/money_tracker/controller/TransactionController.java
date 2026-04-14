package com.rizwan.money_tracker.controller;

import com.rizwan.money_tracker.dto.TransactionDto;
import com.rizwan.money_tracker.entity.TransactionType;
import com.rizwan.money_tracker.service.ExcelService;
import com.rizwan.money_tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ExcelService excelService;

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getTransactions(@RequestParam String type) {
        return ResponseEntity.ok(transactionService.getCurrentMonthTransactions(parseTransactionType(type)));
    }

    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadTransactionExcel(@RequestParam String type) {
        TransactionType transactionType = parseTransactionType(type);
        byte[] excelFile = excelService.exportToExcel(transactionService.getCurrentMonthTransactions(transactionType));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + transactionType.name().toLowerCase(Locale.ROOT) + "_details.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, String>> emailTransactionDetails(@RequestParam String type) {
        TransactionType transactionType = parseTransactionType(type);
        transactionService.emailCurrentMonthTransactionDetails(transactionType);
        return ResponseEntity.ok(Map.of("message", transactionType.name() + " details emailed successfully"));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
        transactionService.delete(transactionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> updateTransaction(@PathVariable Long transactionId, @RequestBody TransactionDto transactionDto) {
        return ResponseEntity.ok(transactionService.update(transactionId, transactionDto));
    }

    private TransactionType parseTransactionType(String type) {
        try {
            return TransactionType.fromValue(type);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type. Must be 'income' or 'expense'.", exception);
        }
    }
}
