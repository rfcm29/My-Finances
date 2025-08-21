package com.example.myfinances.repository;

import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.model.UserInvestmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInvestmentProductRepository extends JpaRepository<UserInvestmentProduct, Long> {
    
    /**
     * Find all products saved by a user
     */
    @Query("SELECT uip FROM UserInvestmentProduct uip " +
           "JOIN FETCH uip.product p " +
           "WHERE uip.user = :user " +
           "ORDER BY uip.savedAt DESC")
    List<UserInvestmentProduct> findByUserOrderBySavedAtDesc(@Param("user") User user);
    
    /**
     * Find user's favorite products
     */
    @Query("SELECT uip FROM UserInvestmentProduct uip " +
           "JOIN FETCH uip.product p " +
           "WHERE uip.user = :user AND uip.isFavorite = true " +
           "ORDER BY uip.savedAt DESC")
    List<UserInvestmentProduct> findByUserAndIsFavoriteTrueOrderBySavedAtDesc(@Param("user") User user);
    
    /**
     * Check if user has saved a specific product
     */
    Optional<UserInvestmentProduct> findByUserAndProduct(@Param("user") User user, @Param("product") InvestmentProduct product);
    
    /**
     * Search user's saved products by name or symbol
     */
    @Query("SELECT uip FROM UserInvestmentProduct uip " +
           "JOIN FETCH uip.product p " +
           "WHERE uip.user = :user AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(p.symbol) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY uip.savedAt DESC")
    List<UserInvestmentProduct> findByUserAndProductNameOrSymbolContainingIgnoreCase(
            @Param("user") User user, @Param("searchTerm") String searchTerm);
    
    /**
     * Find user's saved products by type
     */
    @Query("SELECT uip FROM UserInvestmentProduct uip " +
           "JOIN FETCH uip.product p " +
           "WHERE uip.user = :user AND p.type = :type " +
           "ORDER BY uip.savedAt DESC")
    List<UserInvestmentProduct> findByUserAndProductType(@Param("user") User user, @Param("type") InvestmentProduct.InvestmentType type);
    
    /**
     * Count user's saved products
     */
    long countByUser(@Param("user") User user);
    
    /**
     * Delete user's saved product
     */
    void deleteByUserAndProduct(@Param("user") User user, @Param("product") InvestmentProduct product);
    
    /**
     * Check if exists by user and product
     */
    boolean existsByUserAndProduct(@Param("user") User user, @Param("product") InvestmentProduct product);
}