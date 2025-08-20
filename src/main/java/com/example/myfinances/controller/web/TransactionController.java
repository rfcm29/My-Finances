package com.example.myfinances.controller.web;

import com.example.myfinances.model.Account;
import com.example.myfinances.model.TransactionCategory;
import com.example.myfinances.model.Transaction;
import com.example.myfinances.model.User;
import com.example.myfinances.security.SecurityUtils;
import com.example.myfinances.service.AccountService;
import com.example.myfinances.service.TransactionCategoryService;
import com.example.myfinances.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final TransactionCategoryService transactionCategoryService;

    @GetMapping
    public String listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            Authentication authentication,
            Model model,
            HttpServletRequest request) {
        
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy)
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<Transaction> transactions = transactionService.findTransactionsByUser(user, pageRequest);
        
        // Get filter options
        List<Account> accounts = accountService.findActiveAccountsByUser(user);
        List<TransactionCategory> categories = transactionCategoryService.findCategoriesByUser(user);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("accounts", accounts);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalElements", transactions.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedAccountId", accountId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/transactions/list";
    }

    @GetMapping("/add")
    public String addTransactionForm(Authentication authentication, Model model, HttpServletRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        List<Account> accounts = accountService.findActiveAccountsByUser(user);
        List<TransactionCategory> categories = transactionCategoryService.findCategoriesByUser(user);
        
        model.addAttribute("transactionForm", new TransactionForm());
        model.addAttribute("accounts", accounts);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/transactions/add";
    }

    @PostMapping("/add")
    public String addTransaction(
            @Valid @ModelAttribute("transactionForm") TransactionForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        if (bindingResult.hasErrors()) {
            List<Account> accounts = accountService.findActiveAccountsByUser(user);
            List<TransactionCategory> categories = transactionCategoryService.findCategoriesByUser(user);
            
            model.addAttribute("accounts", accounts);
            model.addAttribute("categories", categories);
            return "pages/transactions/add";
        }

        try {
            Optional<Account> account = accountService.findAccountByIdAndUser(form.getAccountId(), user);
            Optional<TransactionCategory> category = transactionCategoryService.findCategoryByIdAndUser(form.getCategoryId(), user);
            
            if (account.isEmpty()) {
                bindingResult.rejectValue("accountId", "error.accountId", "Conta inválida");
                return "pages/transactions/add";
            }
            
            if (category.isEmpty()) {
                bindingResult.rejectValue("categoryId", "error.categoryId", "Categoria inválida");
                return "pages/transactions/add";
            }
            
            Transaction transaction = Transaction.builder()
                    .account(account.get())
                    .category(category.get())
                    .amount(form.getAmount())
                    .description(form.getDescription())
                    .date(form.getDate())
                    .type(form.getType())
                    .build();
            
            transactionService.createTransaction(transaction);
            
            log.info("Transaction created successfully: {} - {}", transaction.getType(), transaction.getAmount());
            redirectAttributes.addFlashAttribute("message", 
                    "Transação criada com sucesso!");
            
            return "redirect:/transactions";
            
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            model.addAttribute("error", "Ocorreu um erro ao criar a transação. Tente novamente.");
            
            List<Account> accounts = accountService.findActiveAccountsByUser(user);
            List<TransactionCategory> categories = transactionCategoryService.findCategoriesByUser(user);
            
            model.addAttribute("accounts", accounts);
            model.addAttribute("categories", categories);
            return "pages/transactions/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editTransactionForm(@PathVariable Long id, Authentication authentication, 
                                    Model model, HttpServletRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        Optional<Transaction> transaction = transactionService.findTransactionByIdAndUser(id, user);
        if (transaction.isEmpty()) {
            return "redirect:/transactions";
        }
        
        List<Account> accounts = accountService.findActiveAccountsByUser(user);
        List<TransactionCategory> categories = transactionCategoryService.findCategoriesByUser(user);
        
        TransactionForm form = new TransactionForm();
        form.setAccountId(transaction.get().getAccount().getId());
        form.setCategoryId(transaction.get().getCategory().getId());
        form.setAmount(transaction.get().getAmount());
        form.setDescription(transaction.get().getDescription());
        form.setDate(transaction.get().getDate());
        form.setType(transaction.get().getType());
        
        model.addAttribute("transactionForm", form);
        model.addAttribute("transaction", transaction.get());
        model.addAttribute("accounts", accounts);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/transactions/edit";
    }

    @PostMapping("/edit/{id}")
    public String editTransaction(
            @PathVariable Long id,
            @Valid @ModelAttribute("transactionForm") TransactionForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        Optional<Transaction> existingTransaction = transactionService.findTransactionByIdAndUser(id, user);
        if (existingTransaction.isEmpty()) {
            return "redirect:/transactions";
        }
        
        if (bindingResult.hasErrors()) {
            List<Account> accounts = accountService.findActiveAccountsByUser(user);
            List<TransactionCategory> categories = transactionCategoryService.findCategoriesByUser(user);
            
            model.addAttribute("transaction", existingTransaction.get());
            model.addAttribute("accounts", accounts);
            model.addAttribute("categories", categories);
            return "pages/transactions/edit";
        }

        try {
            Optional<Account> account = accountService.findAccountByIdAndUser(form.getAccountId(), user);
            Optional<TransactionCategory> category = transactionCategoryService.findCategoryByIdAndUser(form.getCategoryId(), user);
            
            if (account.isEmpty() || category.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Conta ou categoria inválida");
                return "redirect:/transactions/edit/" + id;
            }
            
            Transaction transaction = existingTransaction.get();
            transaction.setAccount(account.get());
            transaction.setCategory(category.get());
            transaction.setAmount(form.getAmount());
            transaction.setDescription(form.getDescription());
            transaction.setDate(form.getDate());
            transaction.setType(form.getType());
            
            transactionService.updateTransaction(transaction);
            
            log.info("Transaction updated successfully: {}", transaction.getId());
            redirectAttributes.addFlashAttribute("message", 
                    "Transação atualizada com sucesso!");
            
            return "redirect:/transactions";
            
        } catch (Exception e) {
            log.error("Error updating transaction", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao atualizar a transação. Tente novamente.");
            return "redirect:/transactions/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, Authentication authentication, 
                                  RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        try {
            Optional<Transaction> transaction = transactionService.findTransactionByIdAndUser(id, user);
            if (transaction.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Transação não encontrada");
                return "redirect:/transactions";
            }
            
            transactionService.deleteTransaction(transaction.get());
            
            log.info("Transaction deleted successfully: {}", id);
            redirectAttributes.addFlashAttribute("message", "Transação eliminada com sucesso!");
            
        } catch (Exception e) {
            log.error("Error deleting transaction", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao eliminar a transação. Tente novamente.");
        }
        
        return "redirect:/transactions";
    }

    @Data
    public static class TransactionForm {
        @NotNull(message = "A conta é obrigatória")
        private Long accountId;
        
        @NotNull(message = "A categoria é obrigatória")
        private Long categoryId;
        
        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser superior a 0")
        private BigDecimal amount;
        
        @NotBlank(message = "A descrição é obrigatória")
        private String description;
        
        @NotNull(message = "A data é obrigatória")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        
        @NotNull(message = "O tipo é obrigatório")
        private Transaction.TransactionType type;
    }
}