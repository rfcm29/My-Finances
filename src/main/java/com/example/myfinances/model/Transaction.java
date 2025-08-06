package com.example.myfinances.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"account", "category"})
@ToString(exclude = {"account", "category"})
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "Conta é obrigatória")
    private Account account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Categoria é obrigatória")
    private Category category;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Valor é obrigatório")
    private BigDecimal amount;
    
    @Column(length = 500)
    @Size(max = 500, message = "Descrição não pode ter mais de 500 caracteres")
    private String description;
    
    @Column(nullable = false)
    @NotNull(message = "Data é obrigatória")
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Tipo de transação é obrigatório")
    private TransactionType type;
    
    @Column(name = "receipt_url", length = 500)
    @Size(max = 500, message = "URL do comprovante não pode ter mais de 500 caracteres")
    private String receiptUrl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Transaction(Account account, Category category, BigDecimal amount, String description, LocalDate date, TransactionType type) {
        this.account = account;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.type = type;
    }
    
    public enum TransactionType {
        INCOME("Receita"),
        EXPENSE("Despesa"),
        TRANSFER("Transferência");
        
        private final String displayName;
        
        TransactionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
}