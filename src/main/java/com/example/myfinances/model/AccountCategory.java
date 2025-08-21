package com.example.myfinances.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "t_cat_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"subcategories", "accounts"})
@ToString(exclude = {"subcategories", "accounts"})
public class AccountCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    @NotBlank(message = "Código da categoria é obrigatório")
    @Size(max = 20, message = "Código não pode ter mais de 20 caracteres")
    private String code;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(max = 100, message = "Nome não pode ter mais de 100 caracteres")
    private String name;
    
    @Column(length = 500)
    @Size(max = 500, message = "Descrição não pode ter mais de 500 caracteres")
    private String description;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @Column(name = "icon_class", length = 50)
    @Size(max = 50, message = "Classe do ícone não pode ter mais de 50 caracteres")
    private String iconClass;
    
    @Column(name = "color_class", length = 50)
    @Size(max = 50, message = "Classe de cor não pode ter mais de 50 caracteres")
    private String colorClass;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AccountSubcategory> subcategories = new HashSet<>();
    
    @OneToMany(mappedBy = "categoryEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Account> accounts = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public AccountCategory(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
}