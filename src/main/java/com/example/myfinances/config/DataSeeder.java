package com.example.myfinances.config;

import com.example.myfinances.model.Investment;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.service.InvestmentProductService;
import com.example.myfinances.service.InvestmentService;
import com.example.myfinances.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Development data seeder - runs only in dev profile
 * Creates sample investment data for development and testing
 */
@Component
@Profile("disabled")
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final InvestmentProductService investmentProductService;
    private final InvestmentService investmentService;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataSeeder.class);
    
    public DataSeeder(UserService userService, InvestmentProductService investmentProductService, InvestmentService investmentService) {
        this.userService = userService;
        this.investmentProductService = investmentProductService;
        this.investmentService = investmentService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Running development data seeder...");
        
        // Only run if there are users in the system (basic check)
        if (hasExistingUsers()) {
            seedSampleInvestments();
        } else {
            log.info("No users found, skipping investment data seeding");
        }
    }

    private boolean hasExistingUsers() {
        // Simple check - in a real app you might want more sophisticated logic
        try {
            return userService.existsByEmail("test@example.com") || 
                   userService.existsByEmail("admin@example.com");
        } catch (Exception e) {
            log.debug("Error checking for existing users: {}", e.getMessage());
            return false;
        }
    }

    private void seedSampleInvestments() {
        try {
            // Find a test user (you can create one manually for development)
            User testUser = findTestUser();
            if (testUser == null) {
                log.info("No test user found, skipping investment seeding");
                return;
            }

            // Check if investments already exist for this user
            if (investmentService.getInvestmentCount(testUser) > 0) {
                log.info("Investments already exist for test user, skipping seeding");
                return;
            }

            log.info("Creating sample investments for user: {}", testUser.getUsername());
            createSampleInvestments(testUser);
            log.info("Sample investments created successfully");
            
        } catch (Exception e) {
            log.error("Error seeding investment data: {}", e.getMessage(), e);
        }
    }

    private User findTestUser() {
        // Try to find a test user by common test emails
        String[] testEmails = {"test@example.com", "admin@example.com", "user@test.com"};
        
        for (String email : testEmails) {
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                log.info("Found test user with email: {}", email);
                return userOpt.get();
            }
        }
        
        // If no test user found, create one
        log.info("No test user found, creating one for development");
        try {
            return userService.registerUser("Test User", "test@example.com", "password123");
        } catch (Exception e) {
            log.error("Error creating test user: {}", e.getMessage());
            return null;
        }
    }

    private void createSampleInvestments(User user) {
        try {
            // Create sample investment products first
            
            // Sample stocks
            InvestmentProduct appleStock = investmentProductService.createOrUpdateProduct(
                user, "AAPL", "Apple Inc.", "Technology giant and smartphone manufacturer",
                InvestmentProduct.InvestmentType.STOCK, "USD", "NASDAQ", "Technology", "North America"
            );
            // Price will be updated separately via product service
            
            InvestmentProduct microsoftStock = investmentProductService.createOrUpdateProduct(
                user, "MSFT", "Microsoft Corporation", "Software and cloud computing company",
                InvestmentProduct.InvestmentType.STOCK, "USD", "NASDAQ", "Technology", "North America"
            );

            // Sample ETF
            InvestmentProduct vwceEtf = investmentProductService.createOrUpdateProduct(
                user, "VWCE", "Vanguard FTSE All-World UCITS ETF", "Global diversified equity ETF",
                InvestmentProduct.InvestmentType.ETF, "EUR", "XETRA", "Diversified", "Global"
            );

            // Sample cryptocurrency
            InvestmentProduct bitcoin = investmentProductService.createOrUpdateProduct(
                user, "BTC", "Bitcoin", "The first and largest cryptocurrency",
                InvestmentProduct.InvestmentType.CRYPTOCURRENCY, "USD", "Multiple", "Cryptocurrency", "Global"
            );

            // Sample savings account
            InvestmentProduct savingsAccount = investmentProductService.createOrUpdateProduct(
                user, "CGD-POUP", "Conta Poupança CGD - Taxa 2.5%", "Traditional savings account",
                InvestmentProduct.InvestmentType.SAVINGS_ACCOUNT, "EUR", "CGD", "Banking", "Portugal"
            );

            // Sample term deposit
            InvestmentProduct termDeposit = investmentProductService.createOrUpdateProduct(
                user, "CGD-DEP12", "Depósito a Prazo 12 meses - Taxa 3.2%", "12-month term deposit",
                InvestmentProduct.InvestmentType.TERM_DEPOSIT, "EUR", "CGD", "Banking", "Portugal"
            );

            // Now create investment positions
            investmentService.createInvestmentPosition(
                user, appleStock, new BigDecimal("10.000000"), new BigDecimal("150.00"), LocalDate.of(2024, 1, 15)
            );

            investmentService.createInvestmentPosition(
                user, microsoftStock, new BigDecimal("5.000000"), new BigDecimal("380.00"), LocalDate.of(2024, 2, 1)
            );

            investmentService.createInvestmentPosition(
                user, vwceEtf, new BigDecimal("25.000000"), new BigDecimal("85.50"), LocalDate.of(2024, 1, 10)
            );

            investmentService.createInvestmentPosition(
                user, bitcoin, new BigDecimal("0.125000"), new BigDecimal("42000.00"), LocalDate.of(2024, 3, 1)
            );

            investmentService.createInvestmentPosition(
                user, savingsAccount, new BigDecimal("1.000000"), new BigDecimal("5000.00"), LocalDate.of(2023, 12, 1)
            );

            investmentService.createInvestmentPosition(
                user, termDeposit, new BigDecimal("1.000000"), new BigDecimal("10000.00"), LocalDate.of(2024, 1, 1)
            );

            log.info("Created sample investment data for user: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error creating sample investments for user {}: {}", user.getUsername(), e.getMessage(), e);
        }
    }

    private void updateSamplePrices(User user) {
        // This would update current prices to simulate market movements
        // For now, we'll skip this part as it would require more complex logic
        // to find the investments we just created
        
        log.info("Sample price updates would be applied here");
        // In a real implementation, you could:
        // 1. Find investments by symbol
        // 2. Update their current prices to show realistic market movements
        // 3. This would demonstrate the gain/loss calculations
    }
}