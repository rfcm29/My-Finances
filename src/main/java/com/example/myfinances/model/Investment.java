package com.example.myfinances.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "product"})
@ToString(exclude = {"user", "product"})
public class Investment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private InvestmentProduct product;
    
    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal quantity;
    
    @Column(name = "purchase_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal purchasePrice;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    
    @Column(name = "exchange_rate", precision = 15, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (exchangeRate == null) {
            exchangeRate = BigDecimal.ONE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for calculations
    
    /**
     * Calculate total invested amount (quantity * purchase price)
     */
    public BigDecimal getTotalInvested() {
        if (quantity == null || purchasePrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(purchasePrice).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate total invested amount in base currency (EUR)
     */
    public BigDecimal getTotalInvestedInBaseCurrency() {
        if (exchangeRate == null) {
            return getTotalInvested();
        }
        return getTotalInvested().divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate current market value (quantity * current price)
     */
    public BigDecimal getCurrentValue() {
        if (quantity == null || product == null || product.getCurrentPrice() == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(product.getCurrentPrice()).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate current market value in base currency (EUR)
     */
    public BigDecimal getCurrentValueInBaseCurrency() {
        if (exchangeRate == null) {
            return getCurrentValue();
        }
        return getCurrentValue().divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate total gain/loss (current value - total invested)
     */
    public BigDecimal getTotalGainLoss() {
        return getCurrentValue().subtract(getTotalInvested()).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate total gain/loss in base currency
     */
    public BigDecimal getTotalGainLossInBaseCurrency() {
        return getCurrentValueInBaseCurrency().subtract(getTotalInvestedInBaseCurrency()).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate percentage gain/loss
     */
    public BigDecimal getPercentageGainLoss() {
        BigDecimal totalInvested = getTotalInvested();
        if (totalInvested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal gainLoss = getTotalGainLoss();
        return gainLoss.divide(totalInvested, 4, RoundingMode.HALF_UP)
                      .multiply(BigDecimal.valueOf(100))
                      .setScale(2, RoundingMode.HALF_UP);
    }
    
    // Formatting methods for display
    
    public String getFormattedTotalInvested() {
        return String.format("%.2f %s", getTotalInvested(), product != null ? product.getCurrency() : "");
    }
    
    public String getFormattedCurrentValue() {
        return String.format("%.2f %s", getCurrentValue(), product != null ? product.getCurrency() : "");
    }
    
    public String getFormattedTotalGainLoss() {
        BigDecimal gainLoss = getTotalGainLoss();
        String currency = product != null ? product.getCurrency() : "";
        String sign = gainLoss.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s%.2f %s", sign, gainLoss, currency);
    }
    
    public String getFormattedPercentageGainLoss() {
        BigDecimal percentage = getPercentageGainLoss();
        String sign = percentage.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, percentage);
    }
    
    public String getFormattedQuantity() {
        if (quantity == null) return "0";
        
        // For stocks, show up to 6 decimal places but remove trailing zeros
        // For crypto, might need more precision
        if (product != null && product.getType() == InvestmentProduct.InvestmentType.CRYPTOCURRENCY) {
            return quantity.stripTrailingZeros().toPlainString();
        }
        
        // For stocks and other assets, typically show fewer decimals
        if (quantity.compareTo(BigDecimal.ONE) >= 0) {
            return quantity.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        } else {
            return quantity.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        }
    }
    
    public String getFormattedPurchasePrice() {
        if (purchasePrice == null) return "N/A";
        return String.format("%.2f %s", purchasePrice, product != null ? product.getCurrency() : "");
    }
    
    /**
     * Check if this investment is profitable
     */
    public boolean isProfitable() {
        return getTotalGainLoss().compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Get CSS class for gain/loss styling
     */
    public String getGainLossCssClass() {
        BigDecimal gainLoss = getTotalGainLoss();
        if (gainLoss.compareTo(BigDecimal.ZERO) > 0) {
            return "text-success"; // Green for profit
        } else if (gainLoss.compareTo(BigDecimal.ZERO) < 0) {
            return "text-danger";  // Red for loss
        }
        return "text-muted";       // Grey for break-even
    }
}