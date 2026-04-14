package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.CategoryDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.entity.Transaction;
import com.rizwan.money_tracker.entity.TransactionType;
import com.rizwan.money_tracker.repository.CategoryRepository;
import com.rizwan.money_tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    private Category toEntity(CategoryDto dto, Profile profile) {
        return Category.builder()
                .name(dto.getName())
                .type(dto.getType())
                .icon(dto.getIcon())
                .profile(profile)
                .build();
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .profileId(category.getProfile().getId())
                .createdAt(category.getCreatedAt())
                .modifiedAt(category.getModifiedAt())
                .build();
    }

    public CategoryDto saveCategory(CategoryDto dto) {
        Profile profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(dto.getName(), profile.getId())) {
            throw new RuntimeException("Category with name '" + dto.getName() + "' already exists for this profile.");
        }
        Category category = toEntity(dto, profileService.getCurrentProfile());
        return toDto(categoryRepository.save(category));
    }

    public List<CategoryDto> getCategoriesByCurrentProfile() {
        Long profileId = profileService.getCurrentProfile().getId();
        return categoryRepository.getByProfileId(profileId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<CategoryDto> getCategoriesByType(String type) {
        Long profileId = profileService.getCurrentProfile().getId();
        return categoryRepository.getByTypeAndProfileId(type, profileId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CategoryDto updateCategory(CategoryDto dto) {
        Long profileId = profileService.getCurrentProfile().getId();
        Category existingCategory = categoryRepository.findByIdAndProfileId(dto.getId(), profileId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getId()));

        if (categoryRepository.existsByNameAndProfileIdAndIdNot(dto.getName(), profileId, dto.getId())) {
            throw new RuntimeException("Category with name '" + dto.getName() + "' already exists for this profile.");
        }

        TransactionType targetType = parseCategoryType(dto.getType());
        String normalizedType = normalizeCategoryType(targetType);
        boolean typeChanged = existingCategory.getType() == null
                || !existingCategory.getType().equalsIgnoreCase(normalizedType);

        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());
        existingCategory.setType(normalizedType);

        if (typeChanged) {
            List<Transaction> transactions = transactionRepository.findByProfileIdAndCategoryId(profileId, existingCategory.getId());
            for (Transaction transaction : transactions) {
                transaction.setType(targetType);
            }
            transactionRepository.saveAll(transactions);
        }

        return toDto(categoryRepository.save(existingCategory));
    }

    public CategoryDto getCategoryById(Long id) {
        Long profileId = profileService.getCurrentProfile().getId();
        Category category = categoryRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toDto(category);
    }

    private TransactionType parseCategoryType(String type) {
        try {
            return TransactionType.fromValue(type);
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("Invalid category type. Must be 'income' or 'expense'.", exception);
        }
    }

    private String normalizeCategoryType(TransactionType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }

}
