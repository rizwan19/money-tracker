package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.ExpenseDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Expense;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.CategoryRepository;
import com.rizwan.money_tracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
}
