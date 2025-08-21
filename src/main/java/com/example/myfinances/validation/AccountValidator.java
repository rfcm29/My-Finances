package com.example.myfinances.validation;

import com.example.myfinances.exception.ValidationException;
import com.example.myfinances.model.Account;
import com.example.myfinances.model.User;
import com.example.myfinances.service.AccountCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validator for account-related business rules
 */
@Component
@RequiredArgsConstructor
public class AccountValidator {

    private final AccountCategoryService accountCategoryService;

    private static final BigDecimal MAX_BALANCE = new BigDecimal("999999999999.99");
    private static final BigDecimal MIN_BALANCE = new BigDecimal("-999999999999.99");

    public void validateAccountCreation(User user, String name, String category, String subcategory,
                                      BigDecimal balance, String currency) {
        Map<String, String> errors = new HashMap<>();

        // Validate user
        if (user == null) {
            errors.put("user", "User is required");
        }

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Account name is required");
        } else if (name.length() > 100) {
            errors.put("name", "Account name cannot exceed 100 characters");
        }

        // Validate category
        if (category == null || category.trim().isEmpty()) {
            errors.put("category", "Categoria da conta é obrigatória");
        } else {
            List<String> validCategoryNames = accountCategoryService.getCategoryNames();
            if (!validCategoryNames.contains(category)) {
                errors.put("category", "Categoria de conta inválida");
            }
        }
        
        // Validate subcategory (now required since all categories have subcategories)
        if (subcategory == null || subcategory.trim().isEmpty()) {
            errors.put("subcategory", "Subcategoria é obrigatória");
        } else if (category != null) {
            Map<String, List<String>> categoriesWithSubcategories = accountCategoryService.getCategoriesWithSubcategoriesAsStrings();
            List<String> validSubcategories = categoriesWithSubcategories.get(category);
            if (validSubcategories == null || !validSubcategories.contains(subcategory)) {
                errors.put("subcategory", "Subcategoria inválida para a categoria selecionada");
            }
        }

        // Validate balance
        if (balance == null) {
            errors.put("balance", "Initial balance is required");
        } else {
            if (balance.compareTo(MAX_BALANCE) > 0) {
                errors.put("balance", "Balance cannot exceed " + MAX_BALANCE);
            } else if (balance.compareTo(MIN_BALANCE) < 0) {
                errors.put("balance", "Balance cannot be less than " + MIN_BALANCE);
            } else if (balance.scale() > 2) {
                errors.put("balance", "Balance cannot have more than 2 decimal places");
            }
        }

        // Validate currency
        if (currency == null || currency.trim().isEmpty()) {
            errors.put("currency", "Currency is required");
        } else if (currency.length() != 3) {
            errors.put("currency", "Currency must be exactly 3 characters");
        } else if (!currency.equals(currency.toUpperCase())) {
            errors.put("currency", "Currency must be uppercase");
        }

        // Business rule validations
        if (category != null && balance != null) {
            // Credit accounts should typically start with negative or zero balance
            if ("Crédito".equals(category) && balance.compareTo(BigDecimal.ZERO) > 0) {
                errors.put("balance", "Contas de crédito normalmente começam com saldo zero ou negativo");
            }

            // Validate reasonable starting balances
            if (balance.abs().compareTo(new BigDecimal("1000000")) > 0) {
                errors.put("balance", "Saldo inicial excede €1.000.000. Por favor verifique o valor.");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Account validation failed", errors);
        }
    }

    public void validateAccountUpdate(User user, String name, String category, String subcategory,
                                    BigDecimal balance, String currency) {
        // Same validation as creation
        validateAccountCreation(user, name, category, subcategory, balance, currency);
    }

    public void validateBalanceUpdate(Account account, BigDecimal newBalance) {
        Map<String, String> errors = new HashMap<>();

        if (newBalance == null) {
            errors.put("balance", "Balance is required");
        } else {
            if (newBalance.compareTo(MAX_BALANCE) > 0) {
                errors.put("balance", "Balance cannot exceed " + MAX_BALANCE);
            } else if (newBalance.compareTo(MIN_BALANCE) < 0) {
                errors.put("balance", "Balance cannot be less than " + MIN_BALANCE);
            } else if (newBalance.scale() > 2) {
                errors.put("balance", "Balance cannot have more than 2 decimal places");
            }
        }

        // Business rules for specific account categories
        if (account != null && newBalance != null) {
            if ("Contas Bancárias".equals(account.getCategory()) && "Conta Poupança".equals(account.getSubcategory()) && 
                newBalance.compareTo(BigDecimal.ZERO) < 0) {
                errors.put("balance", "Contas poupança não podem ter saldo negativo");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Balance validation failed", errors);
        }
    }

    public void validateTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        Map<String, String> errors = new HashMap<>();

        if (fromAccount == null) {
            errors.put("fromAccount", "Source account is required");
        }

        if (toAccount == null) {
            errors.put("toAccount", "Destination account is required");
        }

        if (amount == null) {
            errors.put("amount", "Transfer amount is required");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("amount", "Transfer amount must be greater than 0");
        } else if (amount.scale() > 2) {
            errors.put("amount", "Transfer amount cannot have more than 2 decimal places");
        }

        // Business rule validations
        if (fromAccount != null && toAccount != null) {
            if (fromAccount.getId().equals(toAccount.getId())) {
                errors.put("accounts", "Cannot transfer to the same account");
            }

            if (!fromAccount.getUser().getId().equals(toAccount.getUser().getId())) {
                errors.put("accounts", "Can only transfer between your own accounts");
            }
        }

        if (fromAccount != null && amount != null) {
            BigDecimal newBalance = fromAccount.getBalance().subtract(amount);
            
            // Check if source account would go below allowed minimum
            if ("Contas Bancárias".equals(fromAccount.getCategory()) && "Conta Poupança".equals(fromAccount.getSubcategory()) && 
                newBalance.compareTo(BigDecimal.ZERO) < 0) {
                errors.put("amount", "Fundos insuficientes na conta poupança");
            }

            // Check for overdraft limits on current accounts
            if ("Contas Bancárias".equals(fromAccount.getCategory()) && "Conta à Ordem".equals(fromAccount.getSubcategory()) && 
                newBalance.compareTo(new BigDecimal("-1000")) < 0) {
                errors.put("amount", "Transferência excederia o limite de descoberto");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Transfer validation failed", errors);
        }
    }
}