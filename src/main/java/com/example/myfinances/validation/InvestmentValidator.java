package com.example.myfinances.validation;

import com.example.myfinances.exception.ValidationException;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Validator for investment-related business rules
 */
@Component
public class InvestmentValidator {

    public void validateInvestmentCreation(User user, InvestmentProduct product, 
                                         BigDecimal quantity, BigDecimal purchasePrice, 
                                         LocalDate purchaseDate, BigDecimal exchangeRate) {
        Map<String, String> errors = new HashMap<>();

        // Validate user
        if (user == null) {
            errors.put("user", "User is required");
        }

        // Validate product
        if (product == null) {
            errors.put("product", "Investment product is required");
        }

        // Validate quantity
        if (quantity == null) {
            errors.put("quantity", "Quantity is required");
        } else if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("quantity", "Quantity must be greater than 0");
        } else if (quantity.scale() > 6) {
            errors.put("quantity", "Quantity cannot have more than 6 decimal places");
        }

        // Validate purchase price
        if (purchasePrice == null) {
            errors.put("purchasePrice", "Purchase price is required");
        } else if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("purchasePrice", "Purchase price must be greater than 0");
        } else if (purchasePrice.scale() > 4) {
            errors.put("purchasePrice", "Purchase price cannot have more than 4 decimal places");
        }

        // Validate purchase date
        if (purchaseDate == null) {
            errors.put("purchaseDate", "Purchase date is required");
        } else if (purchaseDate.isAfter(LocalDate.now())) {
            errors.put("purchaseDate", "Purchase date cannot be in the future");
        } else if (purchaseDate.isBefore(LocalDate.of(1900, 1, 1))) {
            errors.put("purchaseDate", "Purchase date is too far in the past");
        }

        // Validate exchange rate
        if (exchangeRate != null) {
            if (exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
                errors.put("exchangeRate", "Exchange rate must be greater than 0");
            } else if (exchangeRate.scale() > 6) {
                errors.put("exchangeRate", "Exchange rate cannot have more than 6 decimal places");
            }
        }

        // Business rule validations
        if (product != null && quantity != null && purchasePrice != null) {
            BigDecimal totalValue = quantity.multiply(purchasePrice);
            
            // Check for extremely large investments (might be a mistake)
            if (totalValue.compareTo(new BigDecimal("1000000")) > 0) {
                errors.put("totalValue", "Investment value exceeds $1,000,000. Please verify the amounts.");
            }

            // Check for fractional shares on whole-share-only products
            if (product.getType() == InvestmentProduct.InvestmentType.STOCK && 
                quantity.stripTrailingZeros().scale() > 0) {
                // Allow fractional shares for most modern brokers, but warn for very small fractions
                if (quantity.compareTo(new BigDecimal("0.001")) < 0) {
                    errors.put("quantity", "Quantity is too small for a stock investment");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Investment validation failed", errors);
        }
    }

    public void validateInvestmentUpdate(User user, InvestmentProduct product, 
                                       BigDecimal quantity, BigDecimal purchasePrice, 
                                       LocalDate purchaseDate, BigDecimal exchangeRate) {
        // Same validation as creation
        validateInvestmentCreation(user, product, quantity, purchasePrice, purchaseDate, exchangeRate);
    }

    public void validateUserOwnership(User currentUser, User resourceOwner) {
        if (currentUser == null || resourceOwner == null) {
            throw new ValidationException("Invalid user context");
        }
        
        if (!currentUser.getId().equals(resourceOwner.getId())) {
            throw new com.example.myfinances.exception.SecurityException(
                "User does not own this resource", 
                "You can only access your own resources"
            );
        }
    }

    public void validateProductSelection(InvestmentProduct product, User user) {
        if (product == null) {
            throw new ValidationException("Product selection is required");
        }

        // Additional business rules can be added here
        // For example: checking if user has access to certain product types
        // or if product is still available for investment
    }
}