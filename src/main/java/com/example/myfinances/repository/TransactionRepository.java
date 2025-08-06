package com.example.myfinances.repository;

import com.example.myfinances.model.Account;
import com.example.myfinances.model.Category;
import com.example.myfinances.model.Transaction;
import com.example.myfinances.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t WHERE t.account.user = :user ORDER BY t.date DESC, t.createdAt DESC")
    Page<Transaction> findTransactionsByUser(@Param("user") User user, Pageable pageable);
    
    Page<Transaction> findByAccount(Account account, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.user = :user AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    List<Transaction> findTransactionsByDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.user = :user AND t.category = :category ORDER BY t.date DESC")
    List<Transaction> findTransactionsByCategory(@Param("user") User user, @Param("category") Category category);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.user = :user AND (LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY t.date DESC")
    Page<Transaction> searchTransactions(@Param("user") User user, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.user = :user AND t.type = 'INCOME'")
    BigDecimal getTotalIncomeByUser(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.user = :user AND t.type = 'EXPENSE'")
    BigDecimal getTotalExpenseByUser(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.user = :user AND t.category = :category")
    BigDecimal getTotalByUserAndCategory(@Param("user") User user, @Param("category") Category category);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.user = :user AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalByUserDateRangeAndType(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("type") Transaction.TransactionType type);
    
    @Query("SELECT c.name, SUM(t.amount) FROM Transaction t JOIN t.category c WHERE t.account.user = :user AND t.type = 'EXPENSE' AND YEAR(t.date) = :year GROUP BY c.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> getMonthlyExpensesByCategory(@Param("user") User user, @Param("year") int year);
    
    @Query("SELECT MONTH(t.date), SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) FROM Transaction t WHERE t.account.user = :user AND YEAR(t.date) = :year GROUP BY MONTH(t.date) ORDER BY MONTH(t.date)")
    List<Object[]> getMonthlyIncomeVsExpenses(@Param("user") User user, @Param("year") int year);
    
    @Query("SELECT c.name, SUM(t.amount) FROM Transaction t JOIN t.category c WHERE t.account.user = :user AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate GROUP BY c.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> getTopExpenseCategories(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
}