package com.rizwan.money_tracker.repository;

import com.rizwan.money_tracker.entity.Expense;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByProfileIdOrderByDateDesc(Long profileId);

    List<Expense> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

    List<Expense> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(Long profileId, LocalDateTime startDate, LocalDateTime endDate, String name, Sort sort);

    List<Expense> findByProfileIdAndDateBetween(Long profileId, LocalDateTime startDate, LocalDateTime endDate);
}
