package com.example.myfinances.service;

import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.repository.InvestmentProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvestmentProductService {

    private final InvestmentProductRepository productRepository;

    // Basic CRUD Operations
    
    public InvestmentProduct createProduct(User user, String symbol, String name, String description,
                                         InvestmentProduct.InvestmentType type, String currency) {
        log.info("Creating investment product for user {}: {} - {}", user.getId(), symbol, name);
        
        // Check if product already exists
        if (productRepository.existsByUserAndSymbolAndCurrency(user, symbol.toUpperCase(), currency.toUpperCase())) {
            throw new IllegalArgumentException("Produto já existe com esse símbolo e moeda");
        }
        
        InvestmentProduct product = InvestmentProduct.builder()
                .user(user)
                .symbol(symbol.toUpperCase())
                .name(name)
                .description(description)
                .type(type)
                .currency(currency.toUpperCase())
                .isActive(true)
                .build();
        
        return productRepository.save(product);
    }
    
    public InvestmentProduct saveProduct(InvestmentProduct product) {
        log.info("Saving investment product: {}", product.getId());
        return productRepository.save(product);
    }
    
    public InvestmentProduct updateProduct(InvestmentProduct product) {
        log.info("Updating investment product: {}", product.getId());
        return productRepository.save(product);
    }
    
    public void deleteProduct(InvestmentProduct product) {
        log.info("Deactivating investment product: {} - {}", product.getId(), product.getName());
        product.setIsActive(false);
        productRepository.save(product);
    }
    
    public void permanentlyDeleteProduct(InvestmentProduct product) {
        log.info("Permanently deleting investment product: {} - {}", product.getId(), product.getName());
        productRepository.delete(product);
    }
    
    // Query Operations
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findActiveProductsByUser(User user) {
        return productRepository.findActiveProductsByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findProductsByUser(User user) {
        return productRepository.findByUserOrderByNameAsc(user);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findActiveProductsByUserAndType(User user, InvestmentProduct.InvestmentType type) {
        return productRepository.findActiveProductsByUserAndType(user, type);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findActiveProductsByUserAndCurrency(User user, String currency) {
        return productRepository.findActiveProductsByUserAndCurrency(user, currency);
    }
    
    @Transactional(readOnly = true)
    public Optional<InvestmentProduct> findProductByIdAndUser(Long id, User user) {
        return productRepository.findById(id)
                .filter(product -> product.getUser().getId().equals(user.getId()));
    }
    
    @Transactional(readOnly = true)
    public Optional<InvestmentProduct> findProductBySymbolAndCurrency(User user, String symbol, String currency) {
        return productRepository.findByUserAndSymbolAndCurrency(user, symbol.toUpperCase(), currency.toUpperCase());
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findProductsBySymbol(User user, String symbol) {
        return productRepository.findByUserAndSymbol(user, symbol.toUpperCase());
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> searchActiveProducts(User user, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findActiveProductsByUser(user);
        }
        return productRepository.searchActiveProducts(user, searchTerm.trim());
    }
    
    // Analytics and Statistics
    
    @Transactional(readOnly = true)
    public long getActiveProductCount(User user) {
        return productRepository.countActiveProductsByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct.InvestmentType> getDistinctProductTypes(User user) {
        return productRepository.findDistinctTypesByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctCurrencies(User user) {
        return productRepository.findDistinctCurrenciesByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctExchanges(User user) {
        return productRepository.findDistinctExchangesByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctSectors(User user) {
        return productRepository.findDistinctSectorsByUser(user);
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctRegions(User user) {
        return productRepository.findDistinctRegionsByUser(user);
    }
    
    // Price Update Operations
    
    public InvestmentProduct updateProductPrice(Long productId, BigDecimal newPrice, User user) {
        Optional<InvestmentProduct> productOpt = findProductByIdAndUser(productId, user);
        
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Produto não encontrado ou não pertence ao utilizador");
        }
        
        InvestmentProduct product = productOpt.get();
        product.setCurrentPrice(newPrice);
        
        log.info("Updated current price for product {} to {}", productId, newPrice);
        return productRepository.save(product);
    }
    
    public void bulkUpdatePrices(List<InvestmentProduct> products, List<BigDecimal> newPrices) {
        if (products.size() != newPrices.size()) {
            throw new IllegalArgumentException("Listas de produtos e preços devem ter o mesmo tamanho");
        }
        
        for (int i = 0; i < products.size(); i++) {
            InvestmentProduct product = products.get(i);
            BigDecimal newPrice = newPrices.get(i);
            product.setCurrentPrice(newPrice);
        }
        
        productRepository.saveAll(products);
        log.info("Updated current prices for {} products", products.size());
    }
    
    // Utility Methods
    
    @Transactional(readOnly = true)
    public boolean productExists(User user, String symbol, String currency) {
        return productRepository.existsByUserAndSymbolAndCurrency(user, symbol.toUpperCase(), currency.toUpperCase());
    }
    
    @Transactional(readOnly = true)
    public ProductSummary getProductSummary(User user) {
        long totalProducts = getActiveProductCount(user);
        List<InvestmentProduct.InvestmentType> types = getDistinctProductTypes(user);
        List<String> currencies = getDistinctCurrencies(user);
        List<String> exchanges = getDistinctExchanges(user);
        List<String> sectors = getDistinctSectors(user);
        List<String> regions = getDistinctRegions(user);
        
        return ProductSummary.builder()
                .totalProducts(totalProducts)
                .productTypes(types)
                .currencies(currencies)
                .exchanges(exchanges)
                .sectors(sectors)
                .regions(regions)
                .build();
    }
    
    public InvestmentProduct createOrUpdateProduct(User user, String symbol, String name, String description,
                                                 InvestmentProduct.InvestmentType type, String currency,
                                                 String exchange, String sector, String region) {
        Optional<InvestmentProduct> existingProduct = findProductBySymbolAndCurrency(user, symbol, currency);
        
        if (existingProduct.isPresent()) {
            InvestmentProduct product = existingProduct.get();
            product.setName(name);
            product.setDescription(description);
            product.setType(type);
            product.setExchange(exchange);
            product.setSector(sector);
            product.setRegion(region);
            product.setIsActive(true); // Reactivate if it was deactivated
            
            log.info("Updated existing investment product: {} - {}", symbol, name);
            return updateProduct(product);
        } else {
            InvestmentProduct product = InvestmentProduct.builder()
                    .user(user)
                    .symbol(symbol.toUpperCase())
                    .name(name)
                    .description(description)
                    .type(type)
                    .currency(currency.toUpperCase())
                    .exchange(exchange)
                    .sector(sector)
                    .region(region)
                    .isActive(true)
                    .build();
            
            log.info("Created new investment product: {} - {}", symbol, name);
            return saveProduct(product);
        }
    }
    
    // Inner classes for data transfer
    
    @lombok.Builder
    @lombok.Data
    public static class ProductSummary {
        private long totalProducts;
        private List<InvestmentProduct.InvestmentType> productTypes;
        private List<String> currencies;
        private List<String> exchanges;
        private List<String> sectors;
        private List<String> regions;
    }
}