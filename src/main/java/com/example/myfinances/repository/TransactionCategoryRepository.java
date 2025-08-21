package com.example.myfinances.repository;

import com.example.myfinances.model.TransactionCategory;
import com.example.myfinances.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {
    
    List<TransactionCategory> findByUser(User user);
    
    @Query("SELECT c FROM TransactionCategory c WHERE c.user = :user AND c.type = :type ORDER BY c.name ASC")
    List<TransactionCategory> findByUserAndType(@Param("user") User user, @Param("type") TransactionCategory.CategoryType type);
    
    @Query("SELECT c FROM TransactionCategory c WHERE c.user = :user AND c.parent IS NULL ORDER BY c.name ASC")
    List<TransactionCategory> findParentCategories(@Param("user") User user);
    
    @Query("SELECT c FROM TransactionCategory c WHERE c.user = :user AND c.parent = :parent ORDER BY c.name ASC")
    List<TransactionCategory> findSubcategories(@Param("user") User user, @Param("parent") TransactionCategory parent);
}