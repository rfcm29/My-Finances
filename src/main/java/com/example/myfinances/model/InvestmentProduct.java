package com.example.myfinances.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment_products", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"symbol", "currency"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
@ToString(exclude = {"user"})
public class InvestmentProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Utilizador é obrigatório")
    private User user;
    
    @Column(length = 20, nullable = false)
    @NotBlank(message = "Símbolo é obrigatório")
    @Size(max = 20, message = "Símbolo não pode ter mais de 20 caracteres")
    private String symbol;
    
    @Column(nullable = false)
    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(max = 200, message = "Nome não pode ter mais de 200 caracteres")
    private String name;
    
    @Column(length = 500)
    @Size(max = 500, message = "Descrição não pode ter mais de 500 caracteres")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Tipo de investimento é obrigatório")
    private InvestmentType type;
    
    @Column(length = 3, nullable = false)
    @NotBlank(message = "Moeda é obrigatória")
    private String currency;
    
    @Column(name = "current_price", precision = 15, scale = 6)
    private BigDecimal currentPrice;
    
    @Column(length = 100)
    @Size(max = 100, message = "Exchange não pode ter mais de 100 caracteres")
    private String exchange;
    
    @Column(length = 50)
    @Size(max = 50, message = "Setor não pode ter mais de 50 caracteres")
    private String sector;
    
    @Column(length = 50)
    @Size(max = 50, message = "Região não pode ter mais de 50 caracteres")
    private String region;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public InvestmentProduct(User user, String symbol, String name, InvestmentType type, String currency) {
        this.user = user;
        this.symbol = symbol.toUpperCase();
        this.name = name;
        this.type = type;
        this.currency = currency.toUpperCase();
    }
    
    public enum InvestmentType {
        STOCK("Ação", "fas fa-chart-line", "#2563EB"),
        ETF("ETF", "fas fa-layer-group", "#059669"),
        MUTUAL_FUND("Fundo de Investimento", "fas fa-coins", "#DC2626"),
        BOND("Obrigação", "fas fa-certificate", "#7C3AED"),
        SAVINGS_ACCOUNT("Conta Poupança", "fas fa-piggy-bank", "#0891B2"),
        TERM_DEPOSIT("Depósito a Prazo", "fas fa-university", "#EA580C"),
        CRYPTOCURRENCY("Criptomoeda", "fab fa-bitcoin", "#F59E0B"),
        COMMODITY("Commodity", "fas fa-seedling", "#84CC16"),
        REAL_ESTATE("Imobiliário", "fas fa-home", "#EC4899"),
        INDEX("Índice", "fas fa-chart-area", "#6366F1"),
        OTHER("Outro", "fas fa-question", "#6B7280");
        
        private final String displayName;
        private final String icon;
        private final String color;
        
        InvestmentType(String displayName, String icon, String color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getColor() { return color; }
    }
    
    public enum Currency {
        EUR("Euro", "€", "EUR"),
        USD("US Dollar", "$", "USD"),
        GBP("British Pound", "£", "GBP"),
        JPY("Japanese Yen", "¥", "JPY"),
        CHF("Swiss Franc", "Fr", "CHF"),
        CAD("Canadian Dollar", "C$", "CAD"),
        AUD("Australian Dollar", "A$", "AUD"),
        BTC("Bitcoin", "₿", "BTC"),
        ETH("Ethereum", "Ξ", "ETH");
        
        private final String displayName;
        private final String symbol;
        private final String code;
        
        Currency(String displayName, String symbol, String code) {
            this.displayName = displayName;
            this.symbol = symbol;
            this.code = code;
        }
        
        public String getDisplayName() { return displayName; }
        public String getSymbol() { return symbol; }
        public String getCode() { return code; }
        
        public static Currency fromCode(String code) {
            for (Currency currency : values()) {
                if (currency.code.equalsIgnoreCase(code)) {
                    return currency;
                }
            }
            return EUR; // default
        }
    }
    
    // Helper methods
    public String getFullName() {
        return String.format("%s (%s)", name, symbol);
    }
    
    public String getCurrencySymbol() {
        try {
            return Currency.fromCode(currency).getSymbol();
        } catch (Exception e) {
            return currency;
        }
    }
    
    public String getFormattedPrice() {
        if (currentPrice == null) {
            return "N/A";
        }
        return String.format("%.2f %s", currentPrice, getCurrencySymbol());
    }
}