package com.example.myfinances.service;

import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.repository.InvestmentProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvestmentProductService {

    private final InvestmentProductRepository productRepository;
    private final YahooFinanceApiService yahooFinanceApiService;
    
    // API Integration Methods
    
    /**
     * Smart search: First checks database, then searches API if not found
     * This is the preferred method for user searches
     */
    public List<InvestmentProduct> searchProductsSmart(String searchTerm, String region) {
        log.info("Smart searching for products: '{}' in region: {}", searchTerm, region);
        
        // Step 1: Search in database first
        List<InvestmentProduct> databaseResults = searchProducts(searchTerm);
        
        if (!databaseResults.isEmpty()) {
            log.info("Found {} products in database for search term: {}", databaseResults.size(), searchTerm);
            return databaseResults;
        }
        
        // Step 2: If not found in database, search API
        log.info("No products found in database, searching API for: {}", searchTerm);
        
        try {
            // Treat searchTerm as a symbol for API search
            List<String> symbols = List.of(searchTerm.trim().toUpperCase());
            List<InvestmentProduct> apiResults = searchProductsFromApi(symbols, region, true);
            
            if (!apiResults.isEmpty()) {
                log.info("Found {} products from API for search term: {} (saved to database)", 
                        apiResults.size(), searchTerm);
            } else {
                log.info("No products found in API for search term: {}", searchTerm);
            }
            
            return apiResults;
        } catch (Exception e) {
            log.error("Error searching products from API for term: {}", searchTerm, e);
            return List.of(); // Return empty list on API error
        }
    }
    
    /**
     * Smart search by symbol: First checks database, then searches API if not found
     * Specifically designed for symbol-based searches
     */
    public Optional<InvestmentProduct> searchProductBySymbolSmart(String symbol, String currency, String region) {
        log.info("Smart searching for product by symbol: {} ({})", symbol, currency);
        
        // Step 1: Check database first
        Optional<InvestmentProduct> databaseResult = findBySymbolAndCurrency(symbol, currency);
        
        if (databaseResult.isPresent()) {
            log.info("Found product in database: {} ({})", symbol, currency);
            return databaseResult;
        }
        
        // Step 2: If not found in database, search API
        log.info("Product not found in database, searching API for: {} ({})", symbol, currency);
        
        try {
            InvestmentProduct apiResult = getProductFromApi(symbol, region, true);
            if (apiResult != null) {
                log.info("Found product from API: {} (saved to database)", symbol);
                return Optional.of(apiResult);
            } else {
                log.info("No product found in API for symbol: {}", symbol);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error searching product from API for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }
    
    /**
     * Search for products via Yahoo Finance API and optionally save them
     * First checks database for existing products to return persisted entities
     */
    public List<InvestmentProduct> searchProductsFromApi(List<String> symbols, String region, boolean saveToDatabase) {
        log.info("Searching for products from API: {} in region: {}", symbols, region);
        
        List<InvestmentProduct> results = new ArrayList<>();
        List<String> symbolsToSearchFromApi = new ArrayList<>();
        
        // First, check which products already exist in database
        for (String symbol : symbols) {
            String upperSymbol = symbol.trim().toUpperCase();
            
            // Try to find existing product (assume USD currency for now, adjust as needed)
            Optional<InvestmentProduct> existing = findBySymbolAndCurrency(upperSymbol, "USD");
            if (existing.isEmpty() && !"USD".equals("EUR")) {
                // Try EUR if USD not found
                existing = findBySymbolAndCurrency(upperSymbol, "EUR");
            }
            
            if (existing.isPresent()) {
                log.debug("Found existing product in database: {}", upperSymbol);
                results.add(existing.get());
            } else {
                log.debug("Product not in database, will search API: {}", upperSymbol);
                symbolsToSearchFromApi.add(upperSymbol);
            }
        }
        
        // Search API only for products not found in database
        if (!symbolsToSearchFromApi.isEmpty()) {
            List<InvestmentProduct> productsFromApi = yahooFinanceApiService.searchBySymbols(symbolsToSearchFromApi, region);
            
            if (saveToDatabase && !productsFromApi.isEmpty()) {
                // Save or update products in database and add persisted entities to results
                for (InvestmentProduct product : productsFromApi) {
                    InvestmentProduct saved = saveOrUpdateProduct(product);
                    results.add(saved);
                }
                log.info("Saved {} new products to database", productsFromApi.size());
            } else {
                // Add API results without saving
                results.addAll(productsFromApi);
            }
        }
        
        log.info("Returning {} total products ({} from database, {} from API)", 
                results.size(), results.size() - symbolsToSearchFromApi.size(), symbolsToSearchFromApi.size());
        
        return results;
    }
    
    /**
     * Get single product from API
     */
    public InvestmentProduct getProductFromApi(String symbol, String region, boolean saveToDatabase) {
        log.info("Fetching product from API: {} in region: {}", symbol, region);
        
        InvestmentProduct productFromApi = yahooFinanceApiService.getQuote(symbol, region);
        
        if (productFromApi != null && saveToDatabase) {
            productFromApi = saveOrUpdateProduct(productFromApi);
            log.info("Saved product to database: {}", symbol);
        }
        
        return productFromApi;
    }
    
    /**
     * Update prices for existing products using API
     */
    @Async
    public void updateProductPricesFromApi(List<InvestmentProduct> products) {
        if (products.isEmpty()) return;
        
        log.info("Updating prices for {} products", products.size());
        
        try {
            yahooFinanceApiService.updatePrices(products);
            productRepository.saveAll(products);
            log.info("Successfully updated prices for {} products", products.size());
        } catch (Exception e) {
            log.error("Error updating product prices from API", e);
        }
    }
    
    /**
     * Update prices for products that haven't been updated recently
     */
    @Async
    public void updateStaleProductPrices(int hoursThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hoursThreshold);
        List<InvestmentProduct> staleProducts = productRepository.findProductsNeedingUpdate(threshold);
        
        if (!staleProducts.isEmpty()) {
            log.info("Found {} products needing price update", staleProducts.size());
            updateProductPricesFromApi(staleProducts);
        }
    }
    
    // CRUD Operations
    
    public InvestmentProduct saveOrUpdateProduct(InvestmentProduct product) {
        // Check if product already exists by symbol and currency
        Optional<InvestmentProduct> existingProduct = productRepository
                .findBySymbolAndCurrency(product.getSymbol(), product.getCurrency());
        
        if (existingProduct.isPresent()) {
            // Update existing product
            InvestmentProduct existing = existingProduct.get();
            updateProductData(existing, product);
            return productRepository.save(existing);
        } else {
            // Save new product
            return productRepository.save(product);
        }
    }
    
    private void updateProductData(InvestmentProduct existing, InvestmentProduct newData) {
        existing.setName(newData.getName());
        existing.setType(newData.getType());
        existing.setDescription(newData.getDescription());
        existing.setExchange(newData.getExchange());
        existing.setSector(newData.getSector());
        existing.setRegion(newData.getRegion());
        existing.setCurrentPrice(newData.getCurrentPrice());
        existing.setMarketCap(newData.getMarketCap());
        existing.setPeRatio(newData.getPeRatio());
        existing.setDividendYield(newData.getDividendYield());
        existing.setBeta(newData.getBeta());
        existing.setFiftyTwoWeekLow(newData.getFiftyTwoWeekLow());
        existing.setFiftyTwoWeekHigh(newData.getFiftyTwoWeekHigh());
        existing.setAvgVolume(newData.getAvgVolume());
        existing.setLastUpdated(LocalDateTime.now());
    }
    
    @Transactional(readOnly = true)
    public Optional<InvestmentProduct> findById(Long id) {
        return productRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<InvestmentProduct> findBySymbolAndCurrency(String symbol, String currency) {
        return productRepository.findBySymbolAndCurrency(symbol, currency);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findBySymbol(String symbol) {
        return productRepository.findBySymbolIgnoreCaseOrderByCurrency(symbol);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchProducts(searchTerm.trim());
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findByType(InvestmentProduct.InvestmentType type) {
        return productRepository.findByTypeOrderBySymbolAsc(type);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findByExchange(String exchange) {
        return productRepository.findByExchangeIgnoreCaseOrderBySymbolAsc(exchange);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findByCurrency(String currency) {
        return productRepository.findByCurrencyOrderBySymbolAsc(currency);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findBySector(String sector) {
        return productRepository.findBySectorIgnoreCaseOrderBySymbolAsc(sector);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findByRegion(String region) {
        return productRepository.findByRegionIgnoreCaseOrderBySymbolAsc(region);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findMostPopular() {
        return productRepository.findMostPopularProducts();
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct> findRecentlyAdded() {
        return productRepository.findRecentlyAdded();
    }
    
    // Filtering helper methods
    
    @Transactional(readOnly = true)
    public List<InvestmentProduct.InvestmentType> getDistinctTypes() {
        return productRepository.findDistinctTypes();
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctCurrencies() {
        return productRepository.findDistinctCurrencies();
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctExchanges() {
        return productRepository.findDistinctExchanges();
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctSectors() {
        return productRepository.findDistinctSectors();
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctRegions() {
        return productRepository.findDistinctRegions();
    }
    
    // Statistics
    
    @Transactional(readOnly = true)
    public long countByType(InvestmentProduct.InvestmentType type) {
        return productRepository.countByType(type);
    }
    
    @Transactional(readOnly = true)
    public long countByCurrency(String currency) {
        return productRepository.countByCurrency(currency);
    }
    
    @Transactional(readOnly = true)
    public boolean existsBySymbolAndCurrency(String symbol, String currency) {
        return productRepository.existsBySymbolAndCurrency(symbol, currency);
    }
    
    public void deleteProduct(InvestmentProduct product) {
        productRepository.delete(product);
    }
    
    // Batch operations for API data
    
    /**
     * Batch search and save multiple symbols from different regions
     */
    public void batchSearchAndSave(List<String> usSymbols, List<String> europeanSymbols) {
        // Search US symbols
        if (usSymbols != null && !usSymbols.isEmpty()) {
            searchProductsFromApi(usSymbols, "US", true);
        }
        
        // Search European symbols
        if (europeanSymbols != null && !europeanSymbols.isEmpty()) {
            searchProductsFromApi(europeanSymbols, "GB", true); // Using GB as proxy for European markets
        }
    }
    
    /**
     * Get summary statistics about products in the database
     */
    @Transactional(readOnly = true)
    public ProductSummary getProductSummary() {
        long totalProducts = productRepository.count();
        List<InvestmentProduct.InvestmentType> types = getDistinctTypes();
        List<String> currencies = getDistinctCurrencies();
        List<String> exchanges = getDistinctExchanges();
        List<String> sectors = getDistinctSectors();
        List<String> regions = getDistinctRegions();
        
        return ProductSummary.builder()
                .totalProducts(totalProducts)
                .productTypes(types)
                .currencies(currencies)
                .exchanges(exchanges)
                .sectors(sectors)
                .regions(regions)
                .build();
    }
    
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