package com.rizwan.money_tracker.repository;

import com.rizwan.money_tracker.entity.Income;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.profile.id = :profileId")
    BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);

    List<Income> findByProfileIdAndNameContainingIgnoreCase(Long profileId, String name, Sort sort);

    List<Income> findByProfileIdAndDateGreaterThanEqualAndNameContainingIgnoreCase(Long profileId, LocalDate startDate, String name, Sort sort);

    List<Income> findByProfileIdAndDateLessThanEqualAndNameContainingIgnoreCase(Long profileId, LocalDate endDate, String name, Sort sort);

    List<Income> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(Long profileId, LocalDate startDate, LocalDate endDate, String name, Sort sort);

    List<Income> findByProfileIdAndDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);
}
