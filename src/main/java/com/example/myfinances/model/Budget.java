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
@Table(name = "budgets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "category"})
@ToString(exclude = {"user", "category"})
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Utilizador é obrigatório")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Categoria é obrigatória")
    private Category category;
    
    @Column(nullable = false, length = 100)
    @NotNull(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome não pode ter mais de 100 caracteres")
    private String name;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Valor é obrigatório")
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Período é obrigatório")
    private BudgetPeriod period;
    
    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Data de início é obrigatória")
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    @NotNull(message = "Data de fim é obrigatória")
    private LocalDate endDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Budget(User user, Category category, BigDecimal amount, BudgetPeriod period, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public enum BudgetPeriod {
        WEEKLY("Semanal"),
        MONTHLY("Mensal"),
        QUARTERLY("Trimestral"),
        YEARLY("Anual");
        
        private final String displayName;
        
        BudgetPeriod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
}