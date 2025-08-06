package com.example.myfinances.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment_positions")
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
    @NotNull(message = "Utilizador é obrigatório")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Produto de investimento é obrigatório")
    private InvestmentProduct product;
    
    @Column(nullable = false, precision = 15, scale = 6)
    @NotNull(message = "Quantidade é obrigatória")
    private BigDecimal quantity;
    
    @Column(name = "purchase_price", nullable = false, precision = 15, scale = 6)
    @NotNull(message = "Preço de compra é obrigatório")
    private BigDecimal purchasePrice;
    
    @Column(name = "purchase_date", nullable = false)
    @NotNull(message = "Data de compra é obrigatória")
    private LocalDate purchaseDate;
    
    @Column(length = 3, nullable = false)
    @NotNull(message = "Moeda é obrigatória")
    private String currency;
    
    @Column(name = "exchange_rate", precision = 15, scale = 6)
    private BigDecimal exchangeRate; // Rate to EUR at time of purchase
    
    @Column(length = 500)
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currency == null) {
            currency = "EUR";
        }
        if (exchangeRate == null) {
            exchangeRate = BigDecimal.ONE; // Default to 1:1 if not specified
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Investment(User user, InvestmentProduct product, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.currency = product != null ? product.getCurrency() : "EUR";
    }
    
    // Business methods
    public BigDecimal getTotalValue() {
        BigDecimal currentPrice = product != null ? product.getCurrentPrice() : null;
        return quantity.multiply(currentPrice != null ? currentPrice : purchasePrice);
    }
    
    public BigDecimal getTotalCost() {
        return quantity.multiply(purchasePrice);
    }
    
    public BigDecimal getTotalValueInEur() {
        BigDecimal totalValue = getTotalValue();
        if ("EUR".equals(currency)) {
            return totalValue;
        }
        // For non-EUR positions, convert using exchange rate
        return totalValue.multiply(exchangeRate);
    }
    
    public BigDecimal getTotalCostInEur() {
        BigDecimal totalCost = getTotalCost();
        if ("EUR".equals(currency)) {
            return totalCost;
        }
        // For non-EUR positions, convert using exchange rate
        return totalCost.multiply(exchangeRate);
    }
    
    public BigDecimal getGainLoss() {
        return getTotalValue().subtract(getTotalCost());
    }
    
    public BigDecimal getGainLossInEur() {
        return getTotalValueInEur().subtract(getTotalCostInEur());
    }
    
    public BigDecimal getGainLossPercentage() {
        BigDecimal totalCost = getTotalCost();
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getGainLoss().divide(totalCost, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
    
    // Helper methods
    public String getProductName() {
        return product != null ? product.getName() : "N/A";
    }
    
    public String getProductSymbol() {
        return product != null ? product.getSymbol() : "N/A";
    }
    
    public InvestmentProduct.InvestmentType getProductType() {
        return product != null ? product.getType() : null;
    }
    
    public String getCurrencySymbol() {
        try {
            return InvestmentProduct.Currency.fromCode(currency).getSymbol();
        } catch (Exception e) {
            return currency;
        }
    }
    
    public String getFormattedTotalValue() {
        return String.format("%.2f %s", getTotalValue(), getCurrencySymbol());
    }
    
    public String getFormattedTotalCost() {
        return String.format("%.2f %s", getTotalCost(), getCurrencySymbol());
    }
    
}