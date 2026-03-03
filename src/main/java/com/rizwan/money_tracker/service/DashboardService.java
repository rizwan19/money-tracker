package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.ExpenseDto;
import com.rizwan.money_tracker.dto.IncomeDto;
import com.rizwan.money_tracker.dto.RecentTransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboardData() {
        var profile = profileService.getCurrentProfile();
        List<IncomeDto> latestIncomes = incomeService.getLatestFiveIncomes();
        List<ExpenseDto> latestExpenses = expenseService.getLatestFiveExpenses();
        List<RecentTransactionDto> recentTransactions = new ArrayList<>();
        Map<String, Object> returnValue = new HashMap<>();
        List<RecentTransactionDto> incomeInfo = latestIncomes.stream().map(income ->
            RecentTransactionDto.builder()
                    .id(income.getId())
                    .name(income.getName())
                    .date(income.getDate())
                    .profileId(profile.getId())
                    .icon(income.getIcon())
                    .type("INCOME")
                    .createdAt(income.getCreatedAt())
                    .modifiedAt(income.getModifiedAt())
                    .amount(income.getAmount())
                    .build()).toList();
        List<RecentTransactionDto> expenseInfo =
            latestExpenses.stream().map(expense -> RecentTransactionDto.builder()
                    .id(expense.getId())
                    .name(expense.getName())
                    .date(expense.getDate())
                    .profileId(profile.getId())
                    .icon(expense.getIcon())
                    .type("EXPENSE")
                    .createdAt(expense.getCreatedAt())
                    .modifiedAt(expense.getModifiedAt())
                    .amount(expense.getAmount())
                    .build()).toList();
        recentTransactions.addAll(incomeInfo);
        recentTransactions.addAll(expenseInfo);

        returnValue.put("totalBalance", incomeService.getTotalIncome().subtract(expenseService.getTotalExpense()));
        returnValue.put("totalIncome", incomeService.getTotalIncome());
        returnValue.put("totalExpense", expenseService.getTotalExpense());
        returnValue.put("recentFiveExpenses", latestExpenses);
        returnValue.put("recentFiveIncomes", latestIncomes);
        returnValue.put("recentTransactions", recentTransactions.stream().sorted((a, b) -> b.getDate().compareTo(a.getDate())).toList());

        return returnValue;
    }
}
