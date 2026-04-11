package com.rizwan.money_tracker.repository;

import com.rizwan.money_tracker.entity.Expense;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

    List<Expense> findByProfileIdAndNameContainingIgnoreCase(Long profileId, String name, Sort sort);

    List<Expense> findByProfileIdAndDateGreaterThanEqualAndNameContainingIgnoreCase(Long profileId, LocalDate startDate, String name, Sort sort);

    List<Expense> findByProfileIdAndDateLessThanEqualAndNameContainingIgnoreCase(Long profileId, LocalDate endDate, String name, Sort sort);

    List<Expense> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(Long profileId, LocalDate startDate, LocalDate endDate, String name, Sort sort);

    List<Expense> findByProfileIdAndDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);

    List<Expense> findByProfileIdAndDate(Long profileId, LocalDate date);

    Optional<Expense> findByIdAndProfileId(Long id, Long profileId);

    Boolean existsByNameAndProfileIdAndIdNot(String name, Long profileId, Long id);

}
