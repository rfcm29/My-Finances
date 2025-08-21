package com.example.myfinances.service;

import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.model.UserInvestmentProduct;
import com.example.myfinances.repository.UserInvestmentProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserInvestmentProductService {
    
    private final UserInvestmentProductRepository userInvestmentProductRepository;
    
    /**
     * Get all products saved by a user
     */
    public List<UserInvestmentProduct> getUserSavedProducts(User user) {
        return userInvestmentProductRepository.findByUserOrderBySavedAtDesc(user);
    }
    
    /**
     * Get user's favorite products
     */
    public List<UserInvestmentProduct> getUserFavoriteProducts(User user) {
        return userInvestmentProductRepository.findByUserAndIsFavoriteTrueOrderBySavedAtDesc(user);
    }
    
    /**
     * Search user's saved products
     */
    public List<UserInvestmentProduct> searchUserSavedProducts(User user, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getUserSavedProducts(user);
        }
        return userInvestmentProductRepository.findByUserAndProductNameOrSymbolContainingIgnoreCase(user, searchTerm.trim());
    }
    
    /**
     * Get user's saved products by type
     */
    public List<UserInvestmentProduct> getUserSavedProductsByType(User user, InvestmentProduct.InvestmentType type) {
        return userInvestmentProductRepository.findByUserAndProductType(user, type);
    }
    
    /**
     * Check if user has saved a specific product
     */
    public boolean isProductSavedByUser(User user, InvestmentProduct product) {
        return userInvestmentProductRepository.existsByUserAndProduct(user, product);
    }
    
    /**
     * Get specific saved product by user and product
     */
    public Optional<UserInvestmentProduct> getUserSavedProduct(User user, InvestmentProduct product) {
        return userInvestmentProductRepository.findByUserAndProduct(user, product);
    }
    
    /**
     * Save a product to user's profile
     */
    @Transactional
    public UserInvestmentProduct saveProductToUser(User user, InvestmentProduct product, String notes, Boolean isFavorite) {
        // Check if already saved
        Optional<UserInvestmentProduct> existing = userInvestmentProductRepository.findByUserAndProduct(user, product);
        
        if (existing.isPresent()) {
            // Update existing entry
            UserInvestmentProduct userProduct = existing.get();
            if (notes != null) {
                userProduct.setNotes(notes);
            }
            if (isFavorite != null) {
                userProduct.setIsFavorite(isFavorite);
            }
            log.info("Updated saved product {} for user {}", product.getSymbol(), user.getId());
            return userInvestmentProductRepository.save(userProduct);
        } else {
            // Create new entry
            UserInvestmentProduct userProduct = UserInvestmentProduct.builder()
                    .user(user)
                    .product(product)
                    .notes(notes)
                    .isFavorite(isFavorite != null ? isFavorite : false)
                    .build();
            
            log.info("Saved product {} to user {}", product.getSymbol(), user.getId());
            return userInvestmentProductRepository.save(userProduct);
        }
    }
    
    /**
     * Save a product to user's profile with default settings
     */
    @Transactional
    public UserInvestmentProduct saveProductToUser(User user, InvestmentProduct product) {
        return saveProductToUser(user, product, null, false);
    }
    
    /**
     * Remove a product from user's saved products
     */
    @Transactional
    public void removeProductFromUser(User user, InvestmentProduct product) {
        userInvestmentProductRepository.deleteByUserAndProduct(user, product);
        log.info("Removed saved product {} from user {}", product.getSymbol(), user.getId());
    }
    
    /**
     * Toggle favorite status of a saved product
     */
    @Transactional
    public UserInvestmentProduct toggleFavorite(User user, InvestmentProduct product) {
        Optional<UserInvestmentProduct> userProductOpt = userInvestmentProductRepository.findByUserAndProduct(user, product);
        
        if (userProductOpt.isPresent()) {
            UserInvestmentProduct userProduct = userProductOpt.get();
            userProduct.setIsFavorite(!userProduct.getIsFavorite());
            log.info("Toggled favorite status for product {} for user {}", product.getSymbol(), user.getId());
            return userInvestmentProductRepository.save(userProduct);
        } else {
            throw new IllegalStateException("Product not saved by user");
        }
    }
    
    /**
     * Update notes for a saved product
     */
    @Transactional
    public UserInvestmentProduct updateProductNotes(User user, InvestmentProduct product, String notes) {
        Optional<UserInvestmentProduct> userProductOpt = userInvestmentProductRepository.findByUserAndProduct(user, product);
        
        if (userProductOpt.isPresent()) {
            UserInvestmentProduct userProduct = userProductOpt.get();
            userProduct.setNotes(notes);
            log.info("Updated notes for product {} for user {}", product.getSymbol(), user.getId());
            return userInvestmentProductRepository.save(userProduct);
        } else {
            throw new IllegalStateException("Product not saved by user");
        }
    }
    
    /**
     * Get count of saved products by user
     */
    public long getSavedProductsCount(User user) {
        return userInvestmentProductRepository.countByUser(user);
    }
    
    /**
     * Get only the products from user's saved list (helper method)
     */
    public List<InvestmentProduct> getUserSavedProductsList(User user) {
        return userInvestmentProductRepository.findByUserOrderBySavedAtDesc(user)
                .stream()
                .map(UserInvestmentProduct::getProduct)
                .toList();
    }
}