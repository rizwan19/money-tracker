package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.CategoryDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

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
        if (categoryRepository.existsByNameAndProfileId(dto.getName(), dto.getProfileId())) {
            throw new RuntimeException("Category with name '" + dto.getName() + "' already exists for this profile.");
        }
        Category category = toEntity(dto, profileService.getCurrentProfile());
        return toDto(categoryRepository.save(category));
    }

    public List<CategoryDto> getCategoriesByCurrentProfile() {
        Long profileId = profileService.getCurrentProfile().getId();
        return categoryRepository.getByProfilleId(profileId)
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

    public CategoryDto updateCategory(CategoryDto dto) {
        Long profileId = profileService.getCurrentProfile().getId();
        Category existingCategory = categoryRepository.findByIdAndProfileId(dto.getId(), profileId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getId()));
        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());
        existingCategory.setType(dto.getType());
        return toDto(categoryRepository.save(existingCategory));
    }

}
