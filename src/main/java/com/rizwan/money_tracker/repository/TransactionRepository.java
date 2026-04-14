package com.rizwan.money_tracker.repository;

import com.rizwan.money_tracker.entity.Transaction;
import com.rizwan.money_tracker.entity.TransactionType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findTop5ByProfileIdAndTypeOrderByDateDesc(Long profileId, TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.profile.id = :profileId AND t.type = :type")
    BigDecimal sumByProfileIdAndType(@Param("profileId") Long profileId, @Param("type") TransactionType type);

    List<Transaction> findByProfileIdAndTypeAndNameContainingIgnoreCase(Long profileId, TransactionType type, String name, Sort sort);

    List<Transaction> findByProfileIdAndTypeAndDateGreaterThanEqualAndNameContainingIgnoreCase(Long profileId, TransactionType type, LocalDate startDate, String name, Sort sort);

    List<Transaction> findByProfileIdAndTypeAndDateLessThanEqualAndNameContainingIgnoreCase(Long profileId, TransactionType type, LocalDate endDate, String name, Sort sort);

    List<Transaction> findByProfileIdAndTypeAndDateBetweenAndNameContainingIgnoreCase(Long profileId, TransactionType type, LocalDate startDate, LocalDate endDate, String name, Sort sort);

    List<Transaction> findByProfileIdAndTypeAndDateBetween(Long profileId, TransactionType type, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByProfileIdAndTypeAndDate(Long profileId, TransactionType type, LocalDate date);

    Optional<Transaction> findByIdAndProfileId(Long id, Long profileId);

    Boolean existsByNameAndProfileIdAndTypeAndIdNot(String name, Long profileId, TransactionType type, Long id);
}
