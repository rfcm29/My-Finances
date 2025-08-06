package com.example.myfinances.repository;

import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, Long> {
    
    List<InvestmentProduct> findByUserAndIsActiveTrue(User user);
    
    List<InvestmentProduct> findByUserOrderByNameAsc(User user);
    
    @Query("SELECT p FROM InvestmentProduct p WHERE p.user = :user AND p.isActive = true ORDER BY p.name ASC")
    List<InvestmentProduct> findActiveProductsByUser(@Param("user") User user);
    
    @Query("SELECT p FROM InvestmentProduct p WHERE p.user = :user AND p.type = :type AND p.isActive = true ORDER BY p.name ASC")
    List<InvestmentProduct> findActiveProductsByUserAndType(@Param("user") User user, 
                                                           @Param("type") InvestmentProduct.InvestmentType type);
    
    @Query("SELECT p FROM InvestmentProduct p WHERE p.user = :user AND p.currency = :currency AND p.isActive = true ORDER BY p.name ASC")
    List<InvestmentProduct> findActiveProductsByUserAndCurrency(@Param("user") User user, 
                                                               @Param("currency") String currency);
    
    @Query("SELECT p FROM InvestmentProduct p WHERE p.user = :user AND p.symbol = :symbol AND p.currency = :currency")
    Optional<InvestmentProduct> findByUserAndSymbolAndCurrency(@Param("user") User user, 
                                                              @Param("symbol") String symbol, 
                                                              @Param("currency") String currency);
    
    @Query("SELECT p FROM InvestmentProduct p WHERE p.user = :user AND p.symbol = :symbol")
    List<InvestmentProduct> findByUserAndSymbol(@Param("user") User user, @Param("symbol") String symbol);
    
    @Query("SELECT p FROM InvestmentProduct p WHERE p.user = :user AND p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.symbol) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<InvestmentProduct> searchActiveProducts(@Param("user") User user, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(p) FROM InvestmentProduct p WHERE p.user = :user AND p.isActive = true")
    long countActiveProductsByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT p.type FROM InvestmentProduct p WHERE p.user = :user AND p.isActive = true")
    List<InvestmentProduct.InvestmentType> findDistinctTypesByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT p.currency FROM InvestmentProduct p WHERE p.user = :user AND p.isActive = true ORDER BY p.currency")
    List<String> findDistinctCurrenciesByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT p.exchange FROM InvestmentProduct p WHERE p.user = :user AND p.isActive = true AND p.exchange IS NOT NULL ORDER BY p.exchange")
    List<String> findDistinctExchangesByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT p.sector FROM InvestmentProduct p WHERE p.user = :user AND p.isActive = true AND p.sector IS NOT NULL ORDER BY p.sector")
    List<String> findDistinctSectorsByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT p.region FROM InvestmentProduct p WHERE p.user = :user AND p.isActive = true AND p.region IS NOT NULL ORDER BY p.region")
    List<String> findDistinctRegionsByUser(@Param("user") User user);
    
    boolean existsByUserAndSymbolAndCurrency(User user, String symbol, String currency);
}