package com.example.myfinances.repository;

import com.example.myfinances.model.Category;
import com.example.myfinances.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUser(User user);
    
    @Query("SELECT c FROM Category c WHERE c.user = :user AND c.type = :type ORDER BY c.name ASC")
    List<Category> findByUserAndType(@Param("user") User user, @Param("type") Category.CategoryType type);
    
    @Query("SELECT c FROM Category c WHERE c.user = :user AND c.parent IS NULL ORDER BY c.name ASC")
    List<Category> findParentCategories(@Param("user") User user);
    
    @Query("SELECT c FROM Category c WHERE c.user = :user AND c.parent = :parent ORDER BY c.name ASC")
    List<Category> findSubcategories(@Param("user") User user, @Param("parent") Category parent);
}