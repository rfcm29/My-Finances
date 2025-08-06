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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Tipo de conta é obrigatório")
    private AccountType type;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Saldo é obrigatório")
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false, length = 3)
    @NotBlank(message = "Moeda é obrigatória")
    @Size(min = 3, max = 3, message = "Moeda deve ter 3 caracteres")
    private String currency = "EUR";
    
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
    
    public Account(User user, String name, AccountType type, String currency) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.currency = currency;
    }
    
    public enum AccountType {
        CHECKING("Conta à Ordem"),
        SAVINGS("Conta Poupança"),
        CREDIT_CARD("Cartão de Crédito"),
        CASH("Dinheiro"),
        INVESTMENT("Investimento");
        
        private final String displayName;
        
        AccountType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
}