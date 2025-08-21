package com.example.myfinances.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_investment_products", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "product"})
@ToString(exclude = {"user", "product"})
public class UserInvestmentProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private InvestmentProduct product;
    
    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;
    
    @Column(name = "is_favorite")
    private Boolean isFavorite = false;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        savedAt = LocalDateTime.now();
        if (isFavorite == null) {
            isFavorite = false;
        }
    }
}