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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "transactions"})
@ToString(exclude = {"user", "transactions"})
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Utilizador é obrigatório")
    private User user;
    
    @Column(nullable = false)
    @NotBlank(message = "Nome da conta é obrigatório")
    @Size(max = 100, message = "Nome da conta não pode ter mais de 100 caracteres")
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Categoria da conta é obrigatória")
    private AccountCategory categoryEntity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = true)
    private AccountSubcategory subcategoryEntity;
    
    // Legacy string fields for backward compatibility during migration
    @Column(nullable = true)
    @Size(max = 50, message = "Categoria não pode ter mais de 50 caracteres")
    private String category;
    
    @Column(nullable = true)
    @Size(max = 50, message = "Subcategoria não pode ter mais de 50 caracteres")
    private String subcategory;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Saldo é obrigatório")
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false, length = 3)
    @NotBlank(message = "Moeda é obrigatória")
    @Size(min = 3, max = 3, message = "Moeda deve ter 3 caracteres")
    private String currency = "EUR";
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Tipo da conta é obrigatório")
    private String type;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Account(User user, String name, String category, String subcategory, String currency) {
        this.user = user;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.currency = currency;
        // Set type based on legacy category name
        this.type = determineLegacyAccountType(category);
    }
    
    private String determineLegacyAccountType(String categoryName) {
        if (categoryName == null) {
            return "OTHER";
        }
        
        switch (categoryName) {
            case "Contas Bancárias":
                return "CHECKING";
            case "Poupanças do Estado":
                return "SAVINGS";
            case "Crédito":
                return "CREDIT_CARD";
            case "Carteiras Digitais":
                return "OTHER";
            case "Dinheiro":
                return "CASH";
            case "Investimentos":
                return "INVESTMENT";
            default:
                return "OTHER";
        }
    }
    
    public Account(User user, String name, AccountCategory categoryEntity, AccountSubcategory subcategoryEntity, String currency) {
        this.user = user;
        this.name = name;
        this.categoryEntity = categoryEntity;
        this.subcategoryEntity = subcategoryEntity;
        this.currency = currency;
        // Set legacy fields for compatibility
        this.category = categoryEntity != null ? categoryEntity.getName() : null;
        this.subcategory = subcategoryEntity != null ? subcategoryEntity.getName() : null;
        // Set type based on category
        this.type = determineAccountType(categoryEntity);
    }
    
    private String determineAccountType(AccountCategory category) {
        if (category == null) {
            return "OTHER";
        }
        
        switch (category.getCode()) {
            case "BANK":
                return "CHECKING";
            case "STATE_SAVINGS":
                return "SAVINGS";
            case "CREDIT":
                return "CREDIT_CARD";
            case "DIGITAL":
                return "OTHER";
            case "CASH":
                return "CASH";
            case "INVESTMENT":
                return "INVESTMENT";
            default:
                return "OTHER";
        }
    }
    
    // Helper methods for account type checking
    public boolean isBankAccount() {
        if (categoryEntity != null) {
            return "BANK".equals(categoryEntity.getCode());
        }
        return "Contas Bancárias".equals(category);
    }
    
    public boolean isStateSavings() {
        if (categoryEntity != null) {
            return "STATE_SAVINGS".equals(categoryEntity.getCode());
        }
        return "Poupanças do Estado".equals(category);
    }
    
    public boolean isCredit() {
        if (categoryEntity != null) {
            return "CREDIT".equals(categoryEntity.getCode());
        }
        return "Crédito".equals(category);
    }
    
    public boolean isDigitalWallet() {
        if (categoryEntity != null) {
            return "DIGITAL".equals(categoryEntity.getCode());
        }
        return "Carteiras Digitais".equals(category);
    }
    
    public boolean isCash() {
        if (categoryEntity != null) {
            return "CASH".equals(categoryEntity.getCode());
        }
        return "Dinheiro".equals(category);
    }
    
    public boolean isInvestment() {
        if (categoryEntity != null) {
            return "INVESTMENT".equals(categoryEntity.getCode());
        }
        return "Investimentos".equals(category);
    }
    
    public String getDisplayName() {
        if (categoryEntity != null) {
            if (subcategoryEntity != null) {
                return categoryEntity.getName() + " - " + subcategoryEntity.getName();
            }
            return categoryEntity.getName();
        }
        // Legacy fallback
        if (subcategory != null && !subcategory.isEmpty()) {
            return category + " - " + subcategory;
        }
        return category;
    }
    
    // Convenience methods to get category and subcategory names
    public String getCategoryName() {
        return categoryEntity != null ? categoryEntity.getName() : category;
    }
    
    public String getSubcategoryName() {
        return subcategoryEntity != null ? subcategoryEntity.getName() : subcategory;
    }
    
}