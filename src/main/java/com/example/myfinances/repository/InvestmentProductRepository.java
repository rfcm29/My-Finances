package com.example.myfinances.repository;

import com.example.myfinances.model.InvestmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, Long> {
    
    // Find by symbol and currency
    Optional<InvestmentProduct> findBySymbolAndCurrency(String symbol, String currency);
    
    // Check if product exists
    boolean existsBySymbolAndCurrency(String symbol, String currency);
    
    // Find by symbol (all currencies)
    List<InvestmentProduct> findBySymbolIgnoreCaseOrderByCurrency(String symbol);
    
    // Search products by various criteria
    @Query("SELECT p FROM InvestmentProduct p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.symbol) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY p.symbol ASC")
    List<InvestmentProduct> searchProducts(@Param("search") String search);
    
    // Find by type
    List<InvestmentProduct> findByTypeOrderBySymbolAsc(InvestmentProduct.InvestmentType type);
    
    // Find by exchange
    List<InvestmentProduct> findByExchangeIgnoreCaseOrderBySymbolAsc(String exchange);
    
    // Find by currency
    List<InvestmentProduct> findByCurrencyOrderBySymbolAsc(String currency);
    
    // Find by sector
    List<InvestmentProduct> findBySectorIgnoreCaseOrderBySymbolAsc(String sector);
    
    // Find by region
    List<InvestmentProduct> findByRegionIgnoreCaseOrderBySymbolAsc(String region);
    
    // Find products that need price updates (older than specified time)
    @Query("SELECT p FROM InvestmentProduct p WHERE p.lastUpdated < :threshold ORDER BY p.lastUpdated ASC")
    List<InvestmentProduct> findProductsNeedingUpdate(@Param("threshold") LocalDateTime threshold);
    
    // Get distinct values for filtering
    @Query("SELECT DISTINCT p.type FROM InvestmentProduct p ORDER BY p.type")
    List<InvestmentProduct.InvestmentType> findDistinctTypes();
    
    @Query("SELECT DISTINCT p.currency FROM InvestmentProduct p WHERE p.currency IS NOT NULL ORDER BY p.currency")
    List<String> findDistinctCurrencies();
    
    @Query("SELECT DISTINCT p.exchange FROM InvestmentProduct p WHERE p.exchange IS NOT NULL ORDER BY p.exchange")
    List<String> findDistinctExchanges();
    
    @Query("SELECT DISTINCT p.sector FROM InvestmentProduct p WHERE p.sector IS NOT NULL ORDER BY p.sector")
    List<String> findDistinctSectors();
    
    @Query("SELECT DISTINCT p.region FROM InvestmentProduct p WHERE p.region IS NOT NULL ORDER BY p.region")
    List<String> findDistinctRegions();
    
    // Count products
    long countByType(InvestmentProduct.InvestmentType type);
    
    long countByCurrency(String currency);
    
    // Find most popular products (by number of investments)
    @Query("SELECT p FROM InvestmentProduct p JOIN p.investments i GROUP BY p ORDER BY COUNT(i) DESC")
    List<InvestmentProduct> findMostPopularProducts();
    
    // Find recently added products
    @Query("SELECT p FROM InvestmentProduct p ORDER BY p.createdAt DESC")
    List<InvestmentProduct> findRecentlyAdded();
}