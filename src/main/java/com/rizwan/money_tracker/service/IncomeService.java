package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.IncomeDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Income;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.CategoryRepository;
import com.rizwan.money_tracker.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    public IncomeDto addIncome(IncomeDto dto) {
        Profile profile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Income income = toEntity(dto, profile, category);
        return toDto(incomeRepository.save(income));
    }

    private Income toEntity(IncomeDto dto, Profile profile, Category category) {
        return Income.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDto toDto(Income income) {
        return IncomeDto.builder()
                .id(income.getId())
                .name(income.getName())
                .icon(income.getIcon())
                .amount(income.getAmount())
                .categoryId(income.getCategory().getId())
                .categoryName(income.getCategory().getName())
                .date(income.getDate())
                .createdAt(income.getCreatedAt())
                .modifiedAt(income.getModifiedAt())
                .build();
    }

    public List<IncomeDto> getCurrentMonthIncomes() {
        Profile profile = profileService.getCurrentProfile();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.withDayOfMonth((1));
        LocalDateTime endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth());
        List<Income> incomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return incomes.stream().map(this::toDto).toList();
    }

    public void deleteIncome(Long id) {
        Profile profile = profileService.getCurrentProfile();
        Income income = incomeRepository.findById(id).orElseThrow(() -> new RuntimeException("Income not found"));

        if (!income.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        incomeRepository.delete(income);
    }

    public List<IncomeDto> getLatestFiveIncomes() {
        Profile profile = profileService.getCurrentProfile();
        List<Income> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return incomes.stream().map(this::toDto).toList();
    }

    public BigDecimal getTotalIncome() {
        Profile profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return Objects.nonNull(total) ? total : BigDecimal.ZERO;
    }

    public List<IncomeDto> filterIncomes(LocalDateTime startDate, LocalDateTime endDate, String term, Sort sort) {
        Profile profile = profileService.getCurrentProfile();
        List<Income> incomes = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, term, sort);
        return incomes.stream().map(this::toDto).toList();
    }
}
