package com.rizwan.money_tracker.repository;

import com.rizwan.money_tracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> getByProfilleId(Long profileId);
    Optional<Category> findByIdAndProfileId(Long id, Long profileId);
    List<Category> getByTypeAndProfileId(String type, Long profileId);

    Boolean existsByNameAndProfileId(String name, Long profileId);
}
