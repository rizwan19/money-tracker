package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.RecentTransactionDto;
import com.rizwan.money_tracker.dto.TransactionDto;
import com.rizwan.money_tracker.entity.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DashboardService {

    private final TransactionService transactionService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboardData() {
        var profile = profileService.getCurrentProfile();
        List<TransactionDto> latestIncomes = transactionService.getLatestFiveTransactions(TransactionType.INCOME);
        List<TransactionDto> latestExpenses = transactionService.getLatestFiveTransactions(TransactionType.EXPENSE);
        List<RecentTransactionDto> recentTransactions = new ArrayList<>();
        Map<String, Object> returnValue = new HashMap<>();
        List<RecentTransactionDto> incomeInfo = latestIncomes.stream().map(transaction ->
            RecentTransactionDto.builder()
                    .id(transaction.getId())
                    .name(transaction.getName())
                    .date(transaction.getDate())
                    .profileId(profile.getId())
                    .icon(transaction.getIcon())
                    .type(TransactionType.INCOME)
                    .createdAt(transaction.getCreatedAt())
                    .modifiedAt(transaction.getModifiedAt())
                    .amount(transaction.getAmount())
                    .build()).toList();
        List<RecentTransactionDto> expenseInfo =
            latestExpenses.stream().map(transaction -> RecentTransactionDto.builder()
                    .id(transaction.getId())
                    .name(transaction.getName())
                    .date(transaction.getDate())
                    .profileId(profile.getId())
                    .icon(transaction.getIcon())
                    .type(TransactionType.EXPENSE)
                    .createdAt(transaction.getCreatedAt())
                    .modifiedAt(transaction.getModifiedAt())
                    .amount(transaction.getAmount())
                    .build()).toList();
        recentTransactions.addAll(incomeInfo);
        recentTransactions.addAll(expenseInfo);

        returnValue.put("totalBalance", transactionService.getTotalByType(TransactionType.INCOME).subtract(transactionService.getTotalByType(TransactionType.EXPENSE)));
        returnValue.put("totalIncome", transactionService.getTotalByType(TransactionType.INCOME));
        returnValue.put("totalExpense", transactionService.getTotalByType(TransactionType.EXPENSE));
        returnValue.put("recentFiveExpenses", latestExpenses);
        returnValue.put("recentFiveIncomes", latestIncomes);
        returnValue.put("recentTransactions", recentTransactions.stream().sorted((a, b) -> b.getDate().compareTo(a.getDate())).toList());

        return returnValue;
    }
}
