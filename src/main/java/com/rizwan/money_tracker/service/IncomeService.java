package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.IncomeDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Income;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.CategoryRepository;
import com.rizwan.money_tracker.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
