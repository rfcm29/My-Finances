package com.example.myfinances.service;

import com.example.myfinances.model.Account;
import com.example.myfinances.model.TransactionCategory;
import com.example.myfinances.model.Transaction;
import com.example.myfinances.model.User;
import com.example.myfinances.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public Transaction createTransaction(Transaction transaction) {
        return createTransaction(transaction.getAccount(), transaction.getCategory(), 
                transaction.getAmount(), transaction.getDescription(), transaction.getDate(), 
                transaction.getType(), transaction.getReceiptUrl());
    }

    public Transaction createTransaction(Account account, TransactionCategory category, BigDecimal amount, 
                                       String description, LocalDate date, Transaction.TransactionType type, String receiptUrl) {
        log.info("Creating transaction for account ID: {} with amount: {}", account.getId(), amount);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .category(category)
                .amount(amount)
                .description(description)
                .date(date != null ? date : LocalDate.now())
                .type(type)
                .receiptUrl(receiptUrl)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        
        updateAccountBalance(account, amount, type);
        
        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());
        return savedTransaction;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> findTransactionsByUser(User user, Pageable pageable) {
        return transactionRepository.findTransactionsByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> findTransactionsByAccount(Account account, Pageable pageable) {
        return transactionRepository.findByAccount(account, pageable);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findTransactionsByDateRange(user, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByCategory(User user, TransactionCategory category) {
        return transactionRepository.findTransactionsByCategory(user, category);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> searchTransactions(User user, String searchTerm, Pageable pageable) {
        return transactionRepository.searchTransactions(user, searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalIncomeByUser(User user) {
        return transactionRepository.getTotalIncomeByUser(user);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenseByUser(User user) {
        return transactionRepository.getTotalExpenseByUser(user);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalByUserAndCategory(User user, TransactionCategory category) {
        return transactionRepository.getTotalByUserAndCategory(user, category);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTotalByUserAndDateRange(user, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findByIdAndUser(Long transactionId, User user) {
        return transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getAccount().getUser().equals(user));
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findTransactionByIdAndUser(Long transactionId, User user) {
        return findByIdAndUser(transactionId, user);
    }

    public Transaction updateTransaction(Transaction transaction) {
        log.info("Updating transaction ID: {}", transaction.getId());
        
        Transaction existingTransaction = transactionRepository.findById(transaction.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        BigDecimal oldAmount = existingTransaction.getAmount();
        Transaction.TransactionType oldType = existingTransaction.getType();
        Account account = existingTransaction.getAccount();
        
        reverseAccountBalance(account, oldAmount, oldType);
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        
        updateAccountBalance(account, transaction.getAmount(), transaction.getType());
        
        log.info("Transaction updated successfully: {}", updatedTransaction.getId());
        return updatedTransaction;
    }

    public void deleteTransaction(Long transactionId, User user) {
        Transaction transaction = findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or not owned by user"));
        
        reverseAccountBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType());
        
        transactionRepository.delete(transaction);
        log.info("Transaction deleted: {}", transactionId);
    }

    public void deleteTransaction(Transaction transaction) {
        reverseAccountBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType());
        transactionRepository.delete(transaction);
        log.info("Transaction deleted: {}", transaction.getId());
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyExpensesByCategory(User user, int year) {
        return transactionRepository.getMonthlyExpensesByCategory(user, year);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyIncomeVsExpenses(User user, int year) {
        return transactionRepository.getMonthlyIncomeVsExpenses(user, year);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTopExpenseCategories(User user, LocalDate startDate, LocalDate endDate, int limit) {
        return transactionRepository.getTopExpenseCategories(user, startDate, endDate, 
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public BigDecimal getNetWorth(User user) {
        BigDecimal totalIncome = getTotalIncomeByUser(user);
        BigDecimal totalExpenses = getTotalExpenseByUser(user);
        return totalIncome.subtract(totalExpenses);
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyTotal(User user, LocalDate month, Transaction.TransactionType type) {
        LocalDate startDate = month.withDayOfMonth(1);
        LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth());
        
        return transactionRepository.getTotalByUserDateRangeAndType(user, startDate, endDate, type);
    }

    private void updateAccountBalance(Account account, BigDecimal amount, Transaction.TransactionType type) {
        BigDecimal adjustment = type == Transaction.TransactionType.INCOME ? amount : amount.negate();
        accountService.adjustBalance(account.getId(), adjustment);
    }

    private void reverseAccountBalance(Account account, BigDecimal amount, Transaction.TransactionType type) {
        BigDecimal adjustment = type == Transaction.TransactionType.INCOME ? amount.negate() : amount;
        accountService.adjustBalance(account.getId(), adjustment);
    }
}