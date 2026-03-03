package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.ExpenseDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Expense;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.CategoryRepository;
import com.rizwan.money_tracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public ExpenseDto addExpense(ExpenseDto dto) {
        Profile profile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Expense expense = toEntity(dto, profile, category);
        return toDto(expenseRepository.save(expense));
    }

    public List<ExpenseDto> getCurrentMonthExpenses() {
        Profile profile = profileService.getCurrentProfile();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.withDayOfMonth((1));
        LocalDateTime endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth());
        List<Expense> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return expenses.stream().map(this::toDto).toList();
    }

    private Expense toEntity(ExpenseDto dto, Profile profile, Category category) {
        return Expense.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDto toDto(Expense expense) {
        return ExpenseDto.builder()
                .id(expense.getId())
                .name(expense.getName())
                .icon(expense.getIcon())
                .amount(expense.getAmount())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .modifiedAt(expense.getModifiedAt())
                .build();
    }

    public void deleteExpense(Long id) {
        Profile profile = profileService.getCurrentProfile();
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        expenseRepository.delete(expense);
    }

    public List<ExpenseDto> getLatestFiveExpenses() {
        Profile profile = profileService.getCurrentProfile();
        List<Expense> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenses.stream().map(this::toDto).toList();
    }

    public BigDecimal getTotalExpense() {
        Profile profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return Objects.nonNull(total) ? total : BigDecimal.ZERO;
    }

    public List<ExpenseDto> filterExpenses(LocalDateTime startDate, LocalDateTime endDate, String term, Sort sort) {
        Profile profile = profileService.getCurrentProfile();
        List<Expense> expenses = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, term, sort);
        return expenses.stream().map(this::toDto).toList();
    }

    public List<ExpenseDto> getExpensesOnDate(Long profileId, LocalDateTime date) {
        List<Expense> expenses = expenseRepository.findByProfileIdAndDate(profileId, date);
        return expenses.stream().map(this::toDto).toList();
    }
}
