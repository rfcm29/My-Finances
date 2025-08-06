package com.example.myfinances.controller.web;

import com.example.myfinances.model.Account;
import com.example.myfinances.model.Transaction;
import com.example.myfinances.model.User;
import com.example.myfinances.service.AccountService;
import com.example.myfinances.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model, HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        log.debug("Loading dashboard for user: {}", user.getEmail());

        populateDashboardData(user, model);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/dashboard";
    }

    private void populateDashboardData(User user, Model model) {
        List<Account> accounts = accountService.findActiveAccountsByUser(user);
        BigDecimal totalBalance = accountService.getTotalBalance(user);
        
        LocalDate currentMonth = LocalDate.now();
        LocalDate previousMonth = currentMonth.minusMonths(1);
        
        BigDecimal monthlyIncome = transactionService.getMonthlyTotal(user, currentMonth, Transaction.TransactionType.INCOME);
        BigDecimal monthlyExpenses = transactionService.getMonthlyTotal(user, currentMonth, Transaction.TransactionType.EXPENSE);
        
        BigDecimal previousMonthIncome = transactionService.getMonthlyTotal(user, previousMonth, Transaction.TransactionType.INCOME);
        BigDecimal previousMonthExpenses = transactionService.getMonthlyTotal(user, previousMonth, Transaction.TransactionType.EXPENSE);
        
        BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpenses);
        BigDecimal netWorth = transactionService.getNetWorth(user);
        
        Page<Transaction> recentTransactions = transactionService.findTransactionsByUser(
                user, PageRequest.of(0, 10, Sort.by("date").descending().and(Sort.by("createdAt").descending()))
        );
        
        LocalDate startOfMonth = currentMonth.withDayOfMonth(1);
        LocalDate endOfMonth = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
        List<Object[]> topExpenseCategories = transactionService.getTopExpenseCategories(user, startOfMonth, endOfMonth, 5);
        
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance != null ? totalBalance : BigDecimal.ZERO);
        model.addAttribute("monthlyIncome", monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO);
        model.addAttribute("monthlyExpenses", monthlyExpenses != null ? monthlyExpenses : BigDecimal.ZERO);
        model.addAttribute("monthlyNet", monthlyNet);
        model.addAttribute("netWorth", netWorth);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("topExpenseCategories", topExpenseCategories);
        
        model.addAttribute("incomeChange", calculatePercentageChange(previousMonthIncome, monthlyIncome));
        model.addAttribute("expenseChange", calculatePercentageChange(previousMonthExpenses, monthlyExpenses));
        
        model.addAttribute("currentMonthName", currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        model.addAttribute("accountCount", accounts.size());
        model.addAttribute("transactionCount", recentTransactions.getTotalElements());
        
        model.addAttribute("hasData", !accounts.isEmpty() || recentTransactions.hasContent());
    }

    private double calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.equals(BigDecimal.ZERO)) {
            return current != null && !current.equals(BigDecimal.ZERO) ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        
        BigDecimal difference = current.subtract(previous);
        return difference.divide(previous, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}