package com.example.myfinances.repository;

import com.example.myfinances.model.Account;
import com.example.myfinances.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    List<Account> findByUserAndActiveTrue(User user);
    
    List<Account> findByUser(User user);
    
    Optional<Account> findByIdAndUser(Long id, User user);
    
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.active = true ORDER BY a.createdAt DESC")
    List<Account> findActiveAccountsByUser(@Param("user") User user);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user = :user AND a.active = true AND a.category != 'Crédito'")
    BigDecimal getTotalBalanceByUser(@Param("user") User user);
    
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.category = :category AND a.active = true")
    List<Account> findByUserAndCategory(@Param("user") User user, @Param("category") String category);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user = :user AND a.category = :category AND a.active = true")
    BigDecimal getTotalBalanceByUserAndCategory(@Param("user") User user, @Param("category") String category);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user = :user AND a.active = true")
    long countActiveAccountsByUser(@Param("user") User user);
    
    boolean existsByUserAndNameAndActiveTrue(User user, String name);
}