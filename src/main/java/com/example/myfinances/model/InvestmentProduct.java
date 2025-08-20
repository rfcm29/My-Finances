package com.example.myfinances.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "investment_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"investments"})
@ToString(exclude = {"investments"})
public class InvestmentProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestmentType type;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 10)
    private String currency = "USD";
    
    @Column(length = 100)
    private String exchange;
    
    @Column(length = 100)
    private String sector;
    
    @Column(length = 100)
    private String region;
    
    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice;
    
    @Column(name = "market_cap")
    private Long marketCap;
    
    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;
    
    @Column(name = "dividend_yield", precision = 6, scale = 4)
    private BigDecimal dividendYield;
    
    @Column(precision = 6, scale = 3)
    private BigDecimal beta;
    
    @Column(name = "fifty_two_week_low", precision = 15, scale = 4)
    private BigDecimal fiftyTwoWeekLow;
    
    @Column(name = "fifty_two_week_high", precision = 15, scale = 4)
    private BigDecimal fiftyTwoWeekHigh;
    
    @Column(name = "avg_volume")
    private Long avgVolume;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Investment> investments = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum InvestmentType {
        STOCK("Stock"),
        ETF("ETF"),
        MUTUAL_FUND("Mutual Fund"),
        BOND("Bond"),
        SAVINGS_ACCOUNT("Savings Account"),
        TERM_DEPOSIT("Term Deposit"),
        CRYPTOCURRENCY("Cryptocurrency"),
        REAL_ESTATE("Real Estate"),
        OTHER("Other");
        
        private final String displayName;
        
        InvestmentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Helper methods for formatting display values
    public String getFormattedPrice() {
        if (currentPrice == null) return "N/A";
        return String.format("%.2f %s", currentPrice, currency);
    }
    
    public String getFormattedMarketCap() {
        if (marketCap == null) return "N/A";
        if (marketCap >= 1_000_000_000_000L) {
            return String.format("%.1fT", marketCap / 1_000_000_000_000.0);
        } else if (marketCap >= 1_000_000_000L) {
            return String.format("%.1fB", marketCap / 1_000_000_000.0);
        } else if (marketCap >= 1_000_000L) {
            return String.format("%.1fM", marketCap / 1_000_000.0);
        }
        return marketCap.toString();
    }
    
    public String getFormattedDividendYield() {
        if (dividendYield == null) return "N/A";
        return String.format("%.2f%%", dividendYield.multiply(BigDecimal.valueOf(100)));
    }
    
    public String getFormattedPeRatio() {
        if (peRatio == null) return "N/A";
        return peRatio.toString();
    }
    
    public String getFormattedBeta() {
        if (beta == null) return "N/A";
        return beta.toString();
    }
    
    public String getFormattedFiftyTwoWeekRange() {
        if (fiftyTwoWeekLow == null || fiftyTwoWeekHigh == null) return "N/A";
        return String.format("%.2f - %.2f %s", fiftyTwoWeekLow, fiftyTwoWeekHigh, currency);
    }
    
    public String getFormattedVolume() {
        if (avgVolume == null) return "N/A";
        if (avgVolume >= 1_000_000L) {
            return String.format("%.1fM", avgVolume / 1_000_000.0);
        } else if (avgVolume >= 1_000L) {
            return String.format("%.1fK", avgVolume / 1_000.0);
        }
        return avgVolume.toString();
    }
}