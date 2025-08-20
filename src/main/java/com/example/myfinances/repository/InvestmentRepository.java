package com.example.myfinances.repository;

import com.example.myfinances.model.Investment;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    
    // Find investments by user
    List<Investment> findByUserOrderByPurchaseDateDesc(User user);
    
    // Find investments by user with pagination
    @Query("SELECT i FROM Investment i WHERE i.user = :user ORDER BY i.purchaseDate DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.fetchSize", value = "20"),
        @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    Page<Investment> findByUserOrderByPurchaseDateDesc(@Param("user") User user, Pageable pageable);
    
    // Find investment by ID and user (for security)
    Optional<Investment> findByIdAndUser(Long id, User user);
    
    // Find investments by user and product
    List<Investment> findByUserAndProductOrderByPurchaseDateDesc(User user, InvestmentProduct product);
    
    // Find investments by user and product type
    @Query("SELECT i FROM Investment i JOIN i.product p WHERE i.user = :user AND p.type = :type ORDER BY i.purchaseDate DESC")
    List<Investment> findByUserAndProductTypeOrderByPurchaseDateDesc(@Param("user") User user, 
                                                                     @Param("type") InvestmentProduct.InvestmentType type);
    
    // Search investments
    @Query("SELECT i FROM Investment i JOIN i.product p WHERE i.user = :user AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.symbol) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY i.purchaseDate DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.fetchSize", value = "20"),
        @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    List<Investment> searchInvestmentsByUser(@Param("user") User user, @Param("search") String search);
    
    // Search investments with pagination
    @Query("SELECT i FROM Investment i JOIN i.product p WHERE i.user = :user AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.symbol) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY i.purchaseDate DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.fetchSize", value = "20"),
        @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    Page<Investment> searchInvestmentsByUser(@Param("user") User user, @Param("search") String search, Pageable pageable);
    
    // Count investments by user
    long countByUser(User user);
    
    // Count investments by user and type
    @Query("SELECT COUNT(i) FROM Investment i JOIN i.product p WHERE i.user = :user AND p.type = :type")
    long countByUserAndProductType(@Param("user") User user, @Param("type") InvestmentProduct.InvestmentType type);
    
    // Get total invested amount by user (in original currency)
    @Query("SELECT COALESCE(SUM(i.quantity * i.purchasePrice), 0) FROM Investment i WHERE i.user = :user")
    BigDecimal getTotalInvestedByUser(@Param("user") User user);
    
    // Get total invested amount by user and currency
    @Query("SELECT COALESCE(SUM(i.quantity * i.purchasePrice), 0) FROM Investment i JOIN i.product p " +
           "WHERE i.user = :user AND p.currency = :currency")
    BigDecimal getTotalInvestedByUserAndCurrency(@Param("user") User user, @Param("currency") String currency);
    
    // Get total invested amount by user in base currency (EUR)
    @Query("SELECT COALESCE(SUM(i.quantity * i.purchasePrice / i.exchangeRate), 0) FROM Investment i WHERE i.user = :user")
    BigDecimal getTotalInvestedInBaseCurrencyByUser(@Param("user") User user);
    
    // Get portfolio allocation by type
    @Query("SELECT p.type as type, COUNT(i) as count, " +
           "COALESCE(SUM(i.quantity * i.purchasePrice / i.exchangeRate), 0) as totalInvested " +
           "FROM Investment i JOIN i.product p WHERE i.user = :user GROUP BY p.type")
    List<Object[]> getPortfolioAllocationByType(@Param("user") User user);
    
    // Get portfolio allocation by currency
    @Query("SELECT p.currency as currency, COUNT(i) as count, " +
           "COALESCE(SUM(i.quantity * i.purchasePrice), 0) as totalInvested " +
           "FROM Investment i JOIN i.product p WHERE i.user = :user GROUP BY p.currency")
    List<Object[]> getPortfolioAllocationByCurrency(@Param("user") User user);
    
    // Find investments by purchase date range
    List<Investment> findByUserAndPurchaseDateBetweenOrderByPurchaseDateDesc(
            User user, LocalDate startDate, LocalDate endDate);
    
    // Find recent investments
    @Query("SELECT i FROM Investment i WHERE i.user = :user ORDER BY i.createdAt DESC")
    List<Investment> findRecentInvestmentsByUser(@Param("user") User user);
    
    // Get distinct investment types for user
    @Query("SELECT DISTINCT p.type FROM Investment i JOIN i.product p WHERE i.user = :user ORDER BY p.type")
    List<InvestmentProduct.InvestmentType> findDistinctTypesByUser(@Param("user") User user);
    
    // Get distinct currencies for user
    @Query("SELECT DISTINCT p.currency FROM Investment i JOIN i.product p WHERE i.user = :user ORDER BY p.currency")
    List<String> findDistinctCurrenciesByUser(@Param("user") User user);
    
    // Check if user has any investments in a specific product
    boolean existsByUserAndProduct(User user, InvestmentProduct product);
    
    // Find investments that need price updates (products with old prices)
    @Query("SELECT i FROM Investment i JOIN i.product p WHERE i.user = :user AND " +
           "p.lastUpdated < :threshold ORDER BY p.lastUpdated ASC")
    List<Investment> findInvestmentsNeedingPriceUpdate(@Param("user") User user, 
                                                       @Param("threshold") java.time.LocalDateTime threshold);
}