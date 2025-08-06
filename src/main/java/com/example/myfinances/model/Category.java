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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "parent", "subcategories", "transactions", "budgets"})
@ToString(exclude = {"user", "parent", "subcategories", "transactions", "budgets"})
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Utilizador é obrigatório")
    private User user;
    
    @Column(nullable = false)
    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(max = 100, message = "Nome da categoria não pode ter mais de 100 caracteres")
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Tipo de categoria é obrigatório")
    private CategoryType type;
    
    @Column(length = 7)
    @Size(max = 7, message = "Cor deve ter no máximo 7 caracteres")
    private String color = "#6B7280";
    
    @Column(length = 50)
    @Size(max = 50, message = "Ícone deve ter no máximo 50 caracteres")
    private String icon;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> subcategories = new HashSet<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Budget> budgets = new HashSet<>();
    
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
    
    public Category(User user, String name, CategoryType type) {
        this.user = user;
        this.name = name;
        this.type = type;
    }
    
    public Category(User user, String name, CategoryType type, String color, String icon) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.color = color;
        this.icon = icon;
    }
    
    public enum CategoryType {
        INCOME("Receita"),
        EXPENSE("Despesa");
        
        private final String displayName;
        
        CategoryType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
}