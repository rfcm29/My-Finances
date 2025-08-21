package com.example.myfinances.service;

import com.example.myfinances.model.InvestmentProduct;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class YahooFinanceApiService {
    
    @Value("${app.yahoo-finance.api-key:602b2840f8mshbbcfa21b7c1e04ep1f8947jsn610998c71e47}")
    private String apiKey;
    
    @Value("${app.yahoo-finance.api-host:apidojo-yahoo-finance-v1.p.rapidapi.com}")
    private String apiHost;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public YahooFinanceApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Search for investment products by symbols
     */
    public List<InvestmentProduct> searchBySymbols(List<String> symbols, String region) {
        List<InvestmentProduct> products = new ArrayList<>();
        
        try {
            String symbolsParam = String.join("%2C", symbols);
            String url = String.format(
                "https://%s/market/v2/get-quotes?region=%s&symbols=%s",
                apiHost, region != null ? region : "US", symbolsParam
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", apiHost)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                YahooQuoteResponse quoteResponse = objectMapper.readValue(response.body(), YahooQuoteResponse.class);
                
                if (quoteResponse.getQuoteResponse() != null && 
                    quoteResponse.getQuoteResponse().getResult() != null) {
                    
                    for (YahooQuote quote : quoteResponse.getQuoteResponse().getResult()) {
                        InvestmentProduct product = mapToInvestmentProduct(quote);
                        if (product != null) {
                            products.add(product);
                        }
                    }
                }
            } else {
                log.warn("Yahoo Finance API returned status code: {} for symbols: {}", 
                        response.statusCode(), symbols);
            }
            
        } catch (Exception e) {
            log.error("Error fetching data from Yahoo Finance API for symbols: {}", symbols, e);
        }
        
        return products;
    }
    
    /**
     * Get quote for a single symbol
     */
    public InvestmentProduct getQuote(String symbol, String region) {
        List<String> symbols = List.of(symbol);
        List<InvestmentProduct> results = searchBySymbols(symbols, region);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Update prices for existing products
     */
    public void updatePrices(List<InvestmentProduct> products) {
        if (products.isEmpty()) return;
        
        List<String> symbols = products.stream()
                .map(InvestmentProduct::getSymbol)
                .toList();
        
        // Group by region if needed, for now use US as default
        List<InvestmentProduct> updatedProducts = searchBySymbols(symbols, "US");
        
        // Update the existing products with new data
        for (InvestmentProduct existingProduct : products) {
            updatedProducts.stream()
                    .filter(updated -> updated.getSymbol().equals(existingProduct.getSymbol()))
                    .findFirst()
                    .ifPresent(updated -> {
                        existingProduct.setCurrentPrice(updated.getCurrentPrice());
                        existingProduct.setMarketCap(updated.getMarketCap());
                        existingProduct.setPeRatio(updated.getPeRatio());
                        existingProduct.setDividendYield(updated.getDividendYield());
                        existingProduct.setBeta(updated.getBeta());
                        existingProduct.setFiftyTwoWeekLow(updated.getFiftyTwoWeekLow());
                        existingProduct.setFiftyTwoWeekHigh(updated.getFiftyTwoWeekHigh());
                        existingProduct.setAvgVolume(updated.getAvgVolume());
                        existingProduct.setLastUpdated(LocalDateTime.now());
                    });
        }
    }
    
    private InvestmentProduct mapToInvestmentProduct(YahooQuote quote) {
        try {
            InvestmentProduct.InvestmentProductBuilder builder = InvestmentProduct.builder()
                    .symbol(quote.getSymbol())
                    .name(quote.getLongName() != null ? quote.getLongName() : quote.getShortName())
                    .currency(quote.getCurrency())
                    .exchange(quote.getFullExchangeName())
                    .currentPrice(quote.getRegularMarketPrice())
                    .lastUpdated(LocalDateTime.now());
            
            // Set investment type based on quoteType
            InvestmentProduct.InvestmentType type = mapQuoteTypeToInvestmentType(quote.getQuoteType());
            builder.type(type);
            
            // Set additional financial data
            if (quote.getMarketCap() != null) {
                builder.marketCap(quote.getMarketCap());
            }
            
            if (quote.getTrailingPE() != null) {
                builder.peRatio(quote.getTrailingPE());
            }
            
            if (quote.getDividendYield() != null) {
                builder.dividendYield(quote.getDividendYield());
            }
            
            if (quote.getBeta() != null) {
                builder.beta(quote.getBeta());
            }
            
            if (quote.getFiftyTwoWeekLow() != null) {
                builder.fiftyTwoWeekLow(quote.getFiftyTwoWeekLow());
            }
            
            if (quote.getFiftyTwoWeekHigh() != null) {
                builder.fiftyTwoWeekHigh(quote.getFiftyTwoWeekHigh());
            }
            
            if (quote.getAverageDailyVolume3Month() != null) {
                builder.avgVolume(quote.getAverageDailyVolume3Month());
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("Error mapping Yahoo quote to InvestmentProduct: {}", quote.getSymbol(), e);
            return null;
        }
    }
    
    private InvestmentProduct.InvestmentType mapQuoteTypeToInvestmentType(String quoteType) {
        if (quoteType == null) return InvestmentProduct.InvestmentType.OTHER;
        
        return switch (quoteType.toUpperCase()) {
            case "EQUITY" -> InvestmentProduct.InvestmentType.STOCK;
            case "ETF" -> InvestmentProduct.InvestmentType.ETF;
            case "MUTUALFUND" -> InvestmentProduct.InvestmentType.MUTUAL_FUND;
            case "BOND" -> InvestmentProduct.InvestmentType.BOND;
            case "CRYPTOCURRENCY" -> InvestmentProduct.InvestmentType.CRYPTOCURRENCY;
            default -> InvestmentProduct.InvestmentType.OTHER;
        };
    }
    
    // DTOs for Yahoo Finance API response
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YahooQuoteResponse {
        @JsonProperty("quoteResponse")
        private QuoteResponse quoteResponse;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuoteResponse {
        private List<YahooQuote> result;
        private Object error;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YahooQuote {
        private String symbol;
        
        @JsonProperty("longName")
        private String longName;
        
        @JsonProperty("shortName")
        private String shortName;
        
        @JsonProperty("quoteType")
        private String quoteType;
        
        private String currency;
        
        @JsonProperty("fullExchangeName")
        private String fullExchangeName;
        
        @JsonProperty("regularMarketPrice")
        private BigDecimal regularMarketPrice;
        
        @JsonProperty("marketCap")
        private Long marketCap;
        
        @JsonProperty("trailingPE")
        private BigDecimal trailingPE;
        
        @JsonProperty("dividendYield")
        private BigDecimal dividendYield;
        
        private BigDecimal beta;
        
        @JsonProperty("fiftyTwoWeekLow")
        private BigDecimal fiftyTwoWeekLow;
        
        @JsonProperty("fiftyTwoWeekHigh")
        private BigDecimal fiftyTwoWeekHigh;
        
        @JsonProperty("averageDailyVolume3Month")
        private Long averageDailyVolume3Month;
    }
}