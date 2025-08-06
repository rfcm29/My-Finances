package com.example.myfinances.controller.web;

import com.example.myfinances.model.Account;
import com.example.myfinances.model.User;
import com.example.myfinances.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public String listAccounts(Authentication authentication, Model model, HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        
        List<Account> accounts = accountService.findActiveAccountsByUser(user);
        BigDecimal totalBalance = accountService.getTotalBalance(user);
        
        // Get balances by type
        BigDecimal checkingBalance = accountService.getTotalBalanceByType(user, Account.AccountType.CHECKING);
        BigDecimal savingsBalance = accountService.getTotalBalanceByType(user, Account.AccountType.SAVINGS);
        BigDecimal creditBalance = accountService.getTotalBalanceByType(user, Account.AccountType.CREDIT_CARD);
        BigDecimal cashBalance = accountService.getTotalBalanceByType(user, Account.AccountType.CASH);
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance != null ? totalBalance : BigDecimal.ZERO);
        model.addAttribute("checkingBalance", checkingBalance != null ? checkingBalance : BigDecimal.ZERO);
        model.addAttribute("savingsBalance", savingsBalance != null ? savingsBalance : BigDecimal.ZERO);
        model.addAttribute("creditBalance", creditBalance != null ? creditBalance : BigDecimal.ZERO);
        model.addAttribute("cashBalance", cashBalance != null ? cashBalance : BigDecimal.ZERO);
        model.addAttribute("accountCount", accounts.size());
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/accounts/list";
    }

    @GetMapping("/add")
    public String addAccountForm(Model model, HttpServletRequest request) {
        model.addAttribute("accountForm", new AccountForm());
        model.addAttribute("currentPath", request.getRequestURI());
        return "pages/accounts/add";
    }

    @PostMapping("/add")
    public String addAccount(
            @Valid @ModelAttribute("accountForm") AccountForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) authentication.getPrincipal();
        
        if (bindingResult.hasErrors()) {
            return "pages/accounts/add";
        }

        try {
            Account account = accountService.createAccount(
                user, 
                form.getName(), 
                form.getType(), 
                form.getCurrency(), 
                form.getInitialBalance()
            );
            
            log.info("Account created successfully: {}", account.getName());
            redirectAttributes.addFlashAttribute("message", 
                    "Conta '" + account.getName() + "' criada com sucesso!");
            
            return "redirect:/accounts";
            
        } catch (Exception e) {
            log.error("Error creating account", e);
            model.addAttribute("error", "Ocorreu um erro ao criar a conta. Tente novamente.");
            return "pages/accounts/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editAccountForm(@PathVariable Long id, Authentication authentication, 
                                Model model, HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        
        Optional<Account> account = accountService.findAccountByIdAndUser(id, user);
        if (account.isEmpty()) {
            return "redirect:/accounts";
        }
        
        AccountForm form = new AccountForm();
        form.setName(account.get().getName());
        form.setType(account.get().getType());
        form.setCurrency(account.get().getCurrency());
        form.setInitialBalance(account.get().getBalance());
        
        model.addAttribute("accountForm", form);
        model.addAttribute("account", account.get());
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/accounts/edit";
    }

    @PostMapping("/edit/{id}")
    public String editAccount(
            @PathVariable Long id,
            @Valid @ModelAttribute("accountForm") AccountForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) authentication.getPrincipal();
        
        Optional<Account> existingAccount = accountService.findAccountByIdAndUser(id, user);
        if (existingAccount.isEmpty()) {
            return "redirect:/accounts";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("account", existingAccount.get());
            return "pages/accounts/edit";
        }

        try {
            Account account = existingAccount.get();
            account.setName(form.getName());
            account.setType(form.getType());
            account.setCurrency(form.getCurrency());
            
            // Only update balance if it's different (this will create a transaction record)
            if (!account.getBalance().equals(form.getInitialBalance())) {
                accountService.updateBalance(account.getId(), form.getInitialBalance());
            }
            
            accountService.updateAccount(account);
            
            log.info("Account updated successfully: {}", account.getId());
            redirectAttributes.addFlashAttribute("message", 
                    "Conta '" + account.getName() + "' atualizada com sucesso!");
            
            return "redirect:/accounts";
            
        } catch (Exception e) {
            log.error("Error updating account", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao atualizar a conta. Tente novamente.");
            return "redirect:/accounts/edit/" + id;
        }
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateAccount(@PathVariable Long id, Authentication authentication, 
                                  RedirectAttributes redirectAttributes) {
        User user = (User) authentication.getPrincipal();
        
        try {
            accountService.deactivateAccount(id, user);
            
            log.info("Account deactivated successfully: {}", id);
            redirectAttributes.addFlashAttribute("message", "Conta desativada com sucesso!");
            
        } catch (Exception e) {
            log.error("Error deactivating account", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao desativar a conta. Tente novamente.");
        }
        
        return "redirect:/accounts";
    }

    @PostMapping("/reactivate/{id}")
    public String reactivateAccount(@PathVariable Long id, Authentication authentication, 
                                  RedirectAttributes redirectAttributes) {
        User user = (User) authentication.getPrincipal();
        
        try {
            accountService.reactivateAccount(id, user);
            
            log.info("Account reactivated successfully: {}", id);
            redirectAttributes.addFlashAttribute("message", "Conta reativada com sucesso!");
            
        } catch (Exception e) {
            log.error("Error reactivating account", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao reativar a conta. Tente novamente.");
        }
        
        return "redirect:/accounts";
    }

    @GetMapping("/transfer")
    public String transferForm(Authentication authentication, Model model, HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        
        List<Account> accounts = accountService.findActiveAccountsByUser(user);
        
        model.addAttribute("transferForm", new TransferForm());
        model.addAttribute("accounts", accounts);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/accounts/transfer";
    }

    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferForm") TransferForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) authentication.getPrincipal();
        
        if (bindingResult.hasErrors()) {
            List<Account> accounts = accountService.findActiveAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            return "pages/accounts/transfer";
        }

        if (form.getFromAccountId().equals(form.getToAccountId())) {
            bindingResult.rejectValue("toAccountId", "error.toAccountId", 
                    "A conta de destino deve ser diferente da conta de origem");
            
            List<Account> accounts = accountService.findActiveAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            return "pages/accounts/transfer";
        }

        try {
            accountService.transfer(form.getFromAccountId(), form.getToAccountId(), 
                    form.getAmount(), user);
            
            log.info("Transfer completed successfully: {} from {} to {}", 
                    form.getAmount(), form.getFromAccountId(), form.getToAccountId());
            redirectAttributes.addFlashAttribute("message", 
                    "Transferência de " + form.getAmount() + "€ realizada com sucesso!");
            
            return "redirect:/accounts";
            
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("amount", "error.amount", e.getMessage());
            
            List<Account> accounts = accountService.findActiveAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            return "pages/accounts/transfer";
            
        } catch (Exception e) {
            log.error("Error during transfer", e);
            model.addAttribute("error", "Ocorreu um erro durante a transferência. Tente novamente.");
            
            List<Account> accounts = accountService.findActiveAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            return "pages/accounts/transfer";
        }
    }

    @Data
    public static class AccountForm {
        @NotBlank(message = "O nome da conta é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        private String name;
        
        @NotNull(message = "O tipo de conta é obrigatório")
        private Account.AccountType type;
        
        @NotBlank(message = "A moeda é obrigatória")
        @Size(min = 3, max = 3, message = "A moeda deve ter 3 caracteres (ex: EUR)")
        private String currency = "EUR";
        
        @NotNull(message = "O saldo inicial é obrigatório")
        @DecimalMin(value = "0.00", message = "O saldo inicial não pode ser negativo")
        private BigDecimal initialBalance = BigDecimal.ZERO;
    }

    @Data
    public static class TransferForm {
        @NotNull(message = "A conta de origem é obrigatória")
        private Long fromAccountId;
        
        @NotNull(message = "A conta de destino é obrigatória")
        private Long toAccountId;
        
        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser superior a 0")
        private BigDecimal amount;
        
        private String description = "Transferência entre contas";
    }
}