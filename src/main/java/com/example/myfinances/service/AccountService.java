package com.example.myfinances.service;

import com.example.myfinances.model.Account;
import com.example.myfinances.model.User;
import com.example.myfinances.repository.AccountRepository;
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
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(User user, String name, Account.AccountType type, String currency, BigDecimal initialBalance) {
        log.info("Creating account '{}' for user ID: {}", name, user.getId());
        
        Account account = Account.builder()
                .user(user)
                .name(name)
                .type(type)
                .balance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .currency(currency != null ? currency : "EUR")
                .active(true)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with ID: {}", savedAccount.getId());
        
        return savedAccount;
    }

    @Transactional(readOnly = true)
    public List<Account> findActiveAccountsByUser(User user) {
        return accountRepository.findActiveAccountsByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Account> findAccountsByUserAndType(User user, Account.AccountType type) {
        return accountRepository.findByUserAndType(user, type);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByIdAndUser(Long accountId, User user) {
        return accountRepository.findById(accountId)
                .filter(account -> account.getUser().equals(user));
    }

    @Transactional(readOnly = true)
    public Optional<Account> findAccountByIdAndUser(Long accountId, User user) {
        return findByIdAndUser(accountId, user);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(User user) {
        return accountRepository.getTotalBalanceByUser(user);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalanceByType(User user, Account.AccountType type) {
        return accountRepository.getTotalBalanceByUserAndType(user, type);
    }

    public Account updateAccount(Account account) {
        log.info("Updating account ID: {}", account.getId());
        return accountRepository.save(account);
    }

    public void updateBalance(Long accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        log.info("Updating balance for account ID: {} from {} to {}", 
                accountId, account.getBalance(), newBalance);
        
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    public void adjustBalance(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        BigDecimal newBalance = account.getBalance().add(amount);
        log.info("Adjusting balance for account ID: {} by {} (new balance: {})", 
                accountId, amount, newBalance);
        
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    public void deactivateAccount(Long accountId, User user) {
        Account account = findByIdAndUser(accountId, user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found or not owned by user"));
        
        account.setActive(false);
        accountRepository.save(account);
        log.info("Account deactivated: {}", accountId);
    }

    public void reactivateAccount(Long accountId, User user) {
        Account account = findByIdAndUser(accountId, user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found or not owned by user"));
        
        account.setActive(true);
        accountRepository.save(account);
        log.info("Account reactivated: {}", accountId);
    }

    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, User user) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        Account fromAccount = findByIdAndUser(fromAccountId, user)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found or not owned by user"));
        
        Account toAccount = findByIdAndUser(toAccountId, user)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found or not owned by user"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source account");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        log.info("Transfer completed: {} {} from account {} to account {}", 
                amount, fromAccount.getCurrency(), fromAccountId, toAccountId);
    }
}