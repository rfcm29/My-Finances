package com.example.myfinances.repository;

import com.example.myfinances.model.Budget;
import com.example.myfinances.model.TransactionCategory;
import com.example.myfinances.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    List<Budget> findByUserAndActiveTrue(User user);
    
    List<Budget> findByUser(User user);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.active = true ORDER BY b.startDate DESC")
    List<Budget> findActiveByUser(@Param("user") User user);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.category = :category AND b.active = true")
    List<Budget> findByUserAndCategoryAndActiveTrue(@Param("user") User user, @Param("category") TransactionCategory category);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.startDate <= :date AND b.endDate >= :date AND b.active = true")
    List<Budget> findActiveBudgetsByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.category = :category AND b.startDate <= :date AND b.endDate >= :date AND b.active = true")
    Optional<Budget> findActiveBudgetByUserAndCategoryAndDate(@Param("user") User user, @Param("category") TransactionCategory category, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.period = :period AND b.active = true")
    List<Budget> findByUserAndPeriodAndActiveTrue(@Param("user") User user, @Param("period") Budget.BudgetPeriod period);
    
    @Query("SELECT COUNT(b) FROM Budget b WHERE b.user = :user AND b.active = true")
    long countActiveBudgetsByUser(@Param("user") User user);
    
    boolean existsByUserAndCategoryAndActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        User user, TransactionCategory category, LocalDate endDate, LocalDate startDate);
}