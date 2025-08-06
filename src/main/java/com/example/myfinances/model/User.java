package com.example.myfinances.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"accounts", "categories", "budgets", "investments"})
@ToString(exclude = {"password", "accounts", "categories", "budgets", "investments"})
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    private String email;
    
    @Column(nullable = false)
    @NotBlank(message = "Password é obrigatória")
    @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    private String password;
    
    @Column(nullable = false)
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome não pode ter mais de 100 caracteres")
    private String name;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    
    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;
    
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;
    
    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Account> accounts = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> categories = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Budget> budgets = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Investment> investments = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<>();
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
}