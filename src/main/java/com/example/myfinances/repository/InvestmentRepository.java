package com.example.myfinances.repository;

import com.example.myfinances.model.Investment;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    
    List<Investment> findByUser(User user);
    
    @Query("SELECT i FROM Investment i LEFT JOIN FETCH i.product WHERE i.user = :user ORDER BY i.purchaseDate DESC")
    List<Investment> findByUserOrderByPurchaseDateDesc(@Param("user") User user);
    
    @Query("SELECT i FROM Investment i LEFT JOIN FETCH i.product WHERE i.user = :user AND i.product.type = :type ORDER BY i.purchaseDate DESC")
    List<Investment> findByUserAndProductType(@Param("user") User user, @Param("type") InvestmentProduct.InvestmentType type);
    
    @Query("SELECT i FROM Investment i LEFT JOIN FETCH i.product WHERE i.user = :user AND i.product = :product ORDER BY i.purchaseDate DESC")
    List<Investment> findByUserAndProduct(@Param("user") User user, @Param("product") InvestmentProduct product);
    
    @Query("SELECT i FROM Investment i LEFT JOIN FETCH i.product WHERE i.user = :user AND i.product.symbol = :symbol ORDER BY i.purchaseDate DESC")
    List<Investment> findByUserAndProductSymbol(@Param("user") User user, @Param("symbol") String symbol);
    
    @Query("SELECT i FROM Investment i LEFT JOIN FETCH i.product WHERE i.user = :user AND i.currency = :currency ORDER BY i.purchaseDate DESC")
    List<Investment> findByUserAndCurrency(@Param("user") User user, @Param("currency") String currency);
    
    @Query("SELECT COALESCE(SUM(i.quantity * i.purchasePrice), 0) FROM Investment i WHERE i.user = :user")
    BigDecimal getTotalInvestedAmountByUser(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(i.quantity * i.purchasePrice * i.exchangeRate), 0) FROM Investment i WHERE i.user = :user")
    BigDecimal getTotalInvestedAmountInEurByUser(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(i.quantity * COALESCE(i.product.currentPrice, i.purchasePrice)), 0) FROM Investment i WHERE i.user = :user")
    BigDecimal getTotalCurrentValueByUser(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(i.quantity * COALESCE(i.product.currentPrice, i.purchasePrice) * i.exchangeRate), 0) FROM Investment i WHERE i.user = :user")
    BigDecimal getTotalCurrentValueInEurByUser(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(i.quantity * i.purchasePrice), 0) FROM Investment i WHERE i.user = :user AND i.currency = :currency")
    BigDecimal getTotalInvestedAmountByUserAndCurrency(@Param("user") User user, @Param("currency") String currency);
    
    @Query("SELECT COALESCE(SUM(i.quantity * COALESCE(i.product.currentPrice, i.purchasePrice)), 0) FROM Investment i WHERE i.user = :user AND i.currency = :currency")
    BigDecimal getTotalCurrentValueByUserAndCurrency(@Param("user") User user, @Param("currency") String currency);
    
    @Query("SELECT COUNT(i) FROM Investment i WHERE i.user = :user")
    long countInvestmentsByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT i.product.type FROM Investment i WHERE i.user = :user")
    List<InvestmentProduct.InvestmentType> findDistinctProductTypesByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT i.currency FROM Investment i WHERE i.user = :user ORDER BY i.currency")
    List<String> findDistinctCurrenciesByUser(@Param("user") User user);
    
    @Query("SELECT i FROM Investment i LEFT JOIN FETCH i.product WHERE i.user = :user AND " +
           "(LOWER(i.product.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.product.symbol) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Investment> searchByUserAndNameOrSymbolOrNotes(@Param("user") User user, @Param("searchTerm") String searchTerm);
    
    Optional<Investment> findByIdAndUser(Long id, User user);
}