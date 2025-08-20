package com.example.myfinances.service;

import com.example.myfinances.model.Investment;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentProductService investmentProductService;
    
    // CRUD Operations
    
    public Investment createInvestment(User user, InvestmentProduct product, BigDecimal quantity, 
                                     BigDecimal purchasePrice, LocalDate purchaseDate, 
                                     BigDecimal exchangeRate, String notes) {
        
        log.info("Creating investment for user {}: {} shares of {}", 
                user.getId(), quantity, product.getSymbol());
        
        Investment investment = Investment.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .purchasePrice(purchasePrice)
                .purchaseDate(purchaseDate)
                .exchangeRate(exchangeRate != null ? exchangeRate : BigDecimal.ONE)
                .notes(notes)
                .build();
        
        return investmentRepository.save(investment);
    }
    
    public Investment createInvestment(User user, InvestmentProduct product, BigDecimal quantity, 
                                     BigDecimal purchasePrice, LocalDate purchaseDate) {
        return createInvestment(user, product, quantity, purchasePrice, purchaseDate, BigDecimal.ONE, null);
    }
    
    public Investment updateInvestment(Investment investment) {
        log.info("Updating investment: {}", investment.getId());
        return investmentRepository.save(investment);
    }
    
    public void deleteInvestment(Investment investment) {
        log.info("Deleting investment: {}", investment.getId());
        investmentRepository.delete(investment);
    }
    
    // Finder methods
    
    @Transactional(readOnly = true)
    public Optional<Investment> findByIdAndUser(Long id, User user) {
        return investmentRepository.findByIdAndUser(id, user);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findByUser(User user) {
        return investmentRepository.findByUserOrderByPurchaseDateDesc(user);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findByUserAndProduct(User user, InvestmentProduct product) {
        return investmentRepository.findByUserAndProductOrderByPurchaseDateDesc(user, product);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findByUserAndType(User user, InvestmentProduct.InvestmentType type) {
        return investmentRepository.findByUserAndProductTypeOrderByPurchaseDateDesc(user, type);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> searchInvestments(User user, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findByUser(user);
        }
        return investmentRepository.searchInvestmentsByUser(user, searchTerm.trim());
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return investmentRepository.findByUserAndPurchaseDateBetweenOrderByPurchaseDateDesc(user, startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findRecentInvestments(User user) {
        return investmentRepository.findRecentInvestmentsByUser(user);
    }
    
    // Statistics and Analytics
    
    @Transactional(readOnly = true)
    public long getInvestmentCount(User user) {
        return investmentRepository.countByUser(user);
    }
    
    @Transactional(readOnly = true)
    public long getInvestmentCountByType(User user, InvestmentProduct.InvestmentType type) {
        return investmentRepository.countByUserAndProductType(user, type);
    }
    
    @Transactional(readOnly = true)
    public PortfolioSummary getPortfolioSummary(User user) {
        List<Investment> investments = findByUser(user);
        
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal totalInvestedBaseCurrency = BigDecimal.ZERO;
        BigDecimal currentValueBaseCurrency = BigDecimal.ZERO;
        
        for (Investment investment : investments) {
            totalInvested = totalInvested.add(investment.getTotalInvested());
            currentValue = currentValue.add(investment.getCurrentValue());
            totalInvestedBaseCurrency = totalInvestedBaseCurrency.add(investment.getTotalInvestedInBaseCurrency());
            currentValueBaseCurrency = currentValueBaseCurrency.add(investment.getCurrentValueInBaseCurrency());
        }
        
        BigDecimal totalGainLoss = currentValue.subtract(totalInvested);
        BigDecimal totalGainLossBaseCurrency = currentValueBaseCurrency.subtract(totalInvestedBaseCurrency);
        
        BigDecimal percentageGainLoss = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) != 0) {
            percentageGainLoss = totalGainLoss.divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        return PortfolioSummary.builder()
                .totalInvestments(investments.size())
                .totalInvested(totalInvested)
                .currentValue(currentValue)
                .totalInvestedBaseCurrency(totalInvestedBaseCurrency)
                .currentValueBaseCurrency(currentValueBaseCurrency)
                .totalGainLoss(totalGainLoss)
                .totalGainLossBaseCurrency(totalGainLossBaseCurrency)
                .percentageGainLoss(percentageGainLoss)
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<TypeAllocation> getPortfolioAllocationByType(User user) {
        List<Object[]> results = investmentRepository.getPortfolioAllocationByType(user);
        
        return results.stream()
                .map(result -> TypeAllocation.builder()
                        .type((InvestmentProduct.InvestmentType) result[0])
                        .count(((Number) result[1]).longValue())
                        .totalInvested((BigDecimal) result[2])
                        .build())
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<CurrencyAllocation> getPortfolioAllocationByCurrency(User user) {
        List<Object[]> results = investmentRepository.getPortfolioAllocationByCurrency(user);
        
        return results.stream()
                .map(result -> CurrencyAllocation.builder()
                        .currency((String) result[0])
                        .count(((Number) result[1]).longValue())
                        .totalInvested((BigDecimal) result[2])
                        .build())
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct.InvestmentType> getDistinctTypesByUser(User user) {
        return investmentRepository.findDistinctTypesByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctCurrenciesByUser(User user) {
        return investmentRepository.findDistinctCurrenciesByUser(user);
    }
    
    // Price update helpers
    
    @Transactional(readOnly = true)
    public List<Investment> findInvestmentsNeedingPriceUpdate(User user, int hoursThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hoursThreshold);
        return investmentRepository.findInvestmentsNeedingPriceUpdate(user, threshold);
    }
    
    public void updateInvestmentPrices(User user) {
        List<Investment> investments = findInvestmentsNeedingPriceUpdate(user, 4); // 4 hours threshold
        
        if (!investments.isEmpty()) {
            List<InvestmentProduct> products = investments.stream()
                    .map(Investment::getProduct)
                    .distinct()
                    .toList();
            
            investmentProductService.updateProductPricesFromApi(products);
            log.info("Triggered price update for {} products for user {}", products.size(), user.getId());
        }
    }
    
    // Helper methods for validation
    
    @Transactional(readOnly = true)
    public boolean hasInvestmentInProduct(User user, InvestmentProduct product) {
        return investmentRepository.existsByUserAndProduct(user, product);
    }
    
    // Portfolio performance tracking
    
    @Transactional(readOnly = true)
    public List<Investment> getProfitableInvestments(User user) {
        return findByUser(user).stream()
                .filter(Investment::isProfitable)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Investment> getLosingInvestments(User user) {
        return findByUser(user).stream()
                .filter(investment -> !investment.isProfitable() && 
                        investment.getTotalGainLoss().compareTo(BigDecimal.ZERO) < 0)
                .toList();
    }
    
    // DTOs for portfolio analytics
    
    @lombok.Builder
    @lombok.Data
    public static class PortfolioSummary {
        private long totalInvestments;
        private BigDecimal totalInvested;
        private BigDecimal currentValue;
        private BigDecimal totalInvestedBaseCurrency;
        private BigDecimal currentValueBaseCurrency;
        private BigDecimal totalGainLoss;
        private BigDecimal totalGainLossBaseCurrency;
        private BigDecimal percentageGainLoss;
        
        // Formatting helpers
        public String getFormattedTotalInvested() {
            return String.format("%.2f €", totalInvestedBaseCurrency);
        }
        
        public String getFormattedCurrentValue() {
            return String.format("%.2f €", currentValueBaseCurrency);
        }
        
        public String getFormattedTotalGainLoss() {
            String sign = totalGainLossBaseCurrency.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
            return String.format("%s%.2f €", sign, totalGainLossBaseCurrency);
        }
        
        public String getFormattedPercentageGainLoss() {
            String sign = percentageGainLoss.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
            return String.format("%s%.2f%%", sign, percentageGainLoss);
        }
        
        public String getGainLossCssClass() {
            if (totalGainLossBaseCurrency.compareTo(BigDecimal.ZERO) > 0) {
                return "text-success";
            } else if (totalGainLossBaseCurrency.compareTo(BigDecimal.ZERO) < 0) {
                return "text-danger";
            }
            return "text-muted";
        }
    }
    
    @lombok.Builder
    @lombok.Data
    public static class TypeAllocation {
        private InvestmentProduct.InvestmentType type;
        private long count;
        private BigDecimal totalInvested;
        
        public String getFormattedTotalInvested() {
            return String.format("%.2f €", totalInvested);
        }
        
        public String getTypeName() {
            return type != null ? type.getDisplayName() : "Unknown";
        }
    }
    
    @lombok.Builder
    @lombok.Data
    public static class CurrencyAllocation {
        private String currency;
        private long count;
        private BigDecimal totalInvested;
        
        public String getFormattedTotalInvested() {
            return String.format("%.2f %s", totalInvested, currency);
        }
    }
}