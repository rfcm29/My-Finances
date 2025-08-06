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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvestmentService {

    private final InvestmentRepository investmentRepository;

    // Basic CRUD Operations
    
    public Investment createInvestmentPosition(User user, InvestmentProduct product, BigDecimal quantity, 
                                             BigDecimal purchasePrice, LocalDate purchaseDate, 
                                             BigDecimal exchangeRate, String notes) {
        log.info("Creating investment position for user {}: {} - {}", user.getId(), product.getSymbol(), product.getName());
        
        Investment investment = Investment.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .purchasePrice(purchasePrice)
                .purchaseDate(purchaseDate)
                .currency(product.getCurrency())
                .exchangeRate(exchangeRate != null ? exchangeRate : BigDecimal.ONE)
                .notes(notes)
                .build();
        
        return investmentRepository.save(investment);
    }
    
    public Investment createInvestmentPosition(User user, InvestmentProduct product, BigDecimal quantity, 
                                             BigDecimal purchasePrice, LocalDate purchaseDate) {
        return createInvestmentPosition(user, product, quantity, purchasePrice, purchaseDate, BigDecimal.ONE, null);
    }
    
    public Investment saveInvestment(Investment investment) {
        log.info("Saving investment: {}", investment.getId());
        return investmentRepository.save(investment);
    }
    
    public Investment updateInvestment(Investment investment) {
        log.info("Updating investment: {}", investment.getId());
        return investmentRepository.save(investment);
    }
    
    public void deleteInvestment(Investment investment) {
        log.info("Deleting investment: {} - {}", investment.getId(), investment.getProductName());
        investmentRepository.delete(investment);
    }
    
    // Query Operations
    
    @Transactional(readOnly = true)
    public List<Investment> findInvestmentsByUser(User user) {
        return investmentRepository.findByUserOrderByPurchaseDateDesc(user);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findInvestmentsByUserAndProductType(User user, InvestmentProduct.InvestmentType type) {
        return investmentRepository.findByUserAndProductType(user, type);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findInvestmentsByUserAndProduct(User user, InvestmentProduct product) {
        return investmentRepository.findByUserAndProduct(user, product);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findInvestmentsByUserAndCurrency(User user, String currency) {
        return investmentRepository.findByUserAndCurrency(user, currency);
    }
    
    @Transactional(readOnly = true)
    public Optional<Investment> findInvestmentByIdAndUser(Long id, User user) {
        return investmentRepository.findByIdAndUser(id, user);
    }
    
    @Transactional(readOnly = true)
    public List<Investment> searchInvestments(User user, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findInvestmentsByUser(user);
        }
        return investmentRepository.searchByUserAndNameOrSymbolOrNotes(user, searchTerm.trim());
    }
    
    @Transactional(readOnly = true)
    public List<Investment> findInvestmentsByProductSymbol(User user, String symbol) {
        return investmentRepository.findByUserAndProductSymbol(user, symbol);
    }
    
    // Portfolio Analytics
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalInvestedAmount(User user) {
        BigDecimal total = investmentRepository.getTotalInvestedAmountByUser(user);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalInvestedAmountInEur(User user) {
        BigDecimal total = investmentRepository.getTotalInvestedAmountInEurByUser(user);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalCurrentValue(User user) {
        BigDecimal total = investmentRepository.getTotalCurrentValueByUser(user);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalCurrentValueInEur(User user) {
        BigDecimal total = investmentRepository.getTotalCurrentValueInEurByUser(user);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalGainLoss(User user) {
        BigDecimal totalValue = getTotalCurrentValue(user);
        BigDecimal totalCost = getTotalInvestedAmount(user);
        return totalValue.subtract(totalCost);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalGainLossPercentage(User user) {
        BigDecimal totalCost = getTotalInvestedAmount(user);
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal gainLoss = getTotalGainLoss(user);
        return gainLoss.divide(totalCost, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    @Transactional(readOnly = true)
    public long getInvestmentCount(User user) {
        return investmentRepository.countInvestmentsByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct.InvestmentType> getDistinctInvestmentTypes(User user) {
        return investmentRepository.findDistinctProductTypesByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctCurrencies(User user) {
        return investmentRepository.findDistinctCurrenciesByUser(user);
    }
    
    // Currency Operations
    
    public Investment updateExchangeRate(Long investmentId, BigDecimal newExchangeRate, User user) {
        Optional<Investment> investmentOpt = findInvestmentByIdAndUser(investmentId, user);
        
        if (investmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Investment position not found or does not belong to user");
        }
        
        Investment investment = investmentOpt.get();
        investment.setExchangeRate(newExchangeRate);
        
        log.info("Updated exchange rate for investment {} to {}", investmentId, newExchangeRate);
        return investmentRepository.save(investment);
    }
    
    // Portfolio Analysis Methods
    
    @Transactional(readOnly = true)
    public PortfolioSummary getPortfolioSummary(User user) {
        List<Investment> investments = findInvestmentsByUser(user);
        
        BigDecimal totalInvested = getTotalInvestedAmount(user);
        BigDecimal totalValue = getTotalCurrentValue(user);
        BigDecimal totalInvestedEur = getTotalInvestedAmountInEur(user);
        BigDecimal totalValueEur = getTotalCurrentValueInEur(user);
        BigDecimal gainLoss = getTotalGainLoss(user);
        BigDecimal gainLossPercentage = getTotalGainLossPercentage(user);
        long count = getInvestmentCount(user);
        List<InvestmentProduct.InvestmentType> types = getDistinctInvestmentTypes(user);
        List<String> currencies = getDistinctCurrencies(user);
        
        return PortfolioSummary.builder()
                .totalInvestments(count)
                .totalInvestedAmount(totalInvested)
                .totalCurrentValue(totalValue)
                .totalInvestedAmountInEur(totalInvestedEur)
                .totalCurrentValueInEur(totalValueEur)
                .totalGainLoss(gainLoss)
                .totalGainLossPercentage(gainLossPercentage)
                .investmentTypes(types)
                .currencies(currencies)
                .investments(investments)
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<TypeAllocation> getPortfolioAllocationByType(User user) {
        List<InvestmentProduct.InvestmentType> types = getDistinctInvestmentTypes(user);
        BigDecimal totalValue = getTotalCurrentValueInEur(user);
        
        return types.stream()
                .map(type -> {
                    List<Investment> investmentsByType = findInvestmentsByUserAndProductType(user, type);
                    BigDecimal typeValue = investmentsByType.stream()
                            .map(Investment::getTotalValueInEur)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal percentage = totalValue.compareTo(BigDecimal.ZERO) == 0 
                            ? BigDecimal.ZERO 
                            : typeValue.divide(totalValue, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                    
                    return TypeAllocation.builder()
                            .type(type)
                            .value(typeValue)
                            .percentage(percentage)
                            .count(investmentsByType.size())
                            .build();
                })
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<CurrencyAllocation> getPortfolioAllocationByCurrency(User user) {
        List<String> currencies = getDistinctCurrencies(user);
        BigDecimal totalValueInEur = getTotalCurrentValueInEur(user);
        
        return currencies.stream()
                .map(currency -> {
                    List<Investment> investmentsByCurrency = findInvestmentsByUserAndCurrency(user, currency);
                    BigDecimal currencyValue = investmentsByCurrency.stream()
                            .map(Investment::getTotalValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal currencyValueInEur = investmentsByCurrency.stream()
                            .map(Investment::getTotalValueInEur)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal percentage = totalValueInEur.compareTo(BigDecimal.ZERO) == 0 
                            ? BigDecimal.ZERO 
                            : currencyValueInEur.divide(totalValueInEur, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                    
                    return CurrencyAllocation.builder()
                            .currency(currency)
                            .value(currencyValue)
                            .valueInEur(currencyValueInEur)
                            .percentage(percentage)
                            .count(investmentsByCurrency.size())
                            .build();
                })
                .toList();
    }
    
    // Inner classes for data transfer
    
    @lombok.Builder
    @lombok.Data
    public static class PortfolioSummary {
        private long totalInvestments;
        private BigDecimal totalInvestedAmount;
        private BigDecimal totalCurrentValue;
        private BigDecimal totalInvestedAmountInEur;
        private BigDecimal totalCurrentValueInEur;
        private BigDecimal totalGainLoss;
        private BigDecimal totalGainLossPercentage;
        private List<InvestmentProduct.InvestmentType> investmentTypes;
        private List<String> currencies;
        private List<Investment> investments;
    }
    
    @lombok.Builder
    @lombok.Data
    public static class TypeAllocation {
        private InvestmentProduct.InvestmentType type;
        private BigDecimal value;
        private BigDecimal percentage;
        private int count;
    }
    
    @lombok.Builder
    @lombok.Data
    public static class CurrencyAllocation {
        private String currency;
        private BigDecimal value;
        private BigDecimal valueInEur;
        private BigDecimal percentage;
        private int count;
    }
}