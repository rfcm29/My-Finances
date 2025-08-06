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
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user = :user AND a.active = true AND a.type != 'CREDIT_CARD'")
    BigDecimal getTotalBalanceByUser(@Param("user") User user);
    
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.type = :type AND a.active = true")
    List<Account> findByUserAndType(@Param("user") User user, @Param("type") Account.AccountType type);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user = :user AND a.type = :type AND a.active = true")
    BigDecimal getTotalBalanceByUserAndType(@Param("user") User user, @Param("type") Account.AccountType type);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user = :user AND a.active = true")
    long countActiveAccountsByUser(@Param("user") User user);
    
    boolean existsByUserAndNameAndActiveTrue(User user, String name);
}