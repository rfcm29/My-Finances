package com.example.myfinances.controller.web;

import com.example.myfinances.model.User;
import com.example.myfinances.service.TransactionCategoryService;
import com.example.myfinances.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final TransactionCategoryService transactionCategoryService;

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "pages/index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model, Authentication authentication) {
        
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        
        if (error != null) {
            model.addAttribute("error", "Email ou password inválidos");
        }
        if (logout != null) {
            model.addAttribute("message", "Logout realizado com sucesso");
        }
        
        return "pages/auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("registrationForm", new RegistrationForm());
        return "pages/auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        
        if (bindingResult.hasErrors()) {
            return "pages/auth/register";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", 
                    "As passwords não coincidem");
            return "pages/auth/register";
        }

        try {
            if (userService.existsByEmail(form.getEmail())) {
                bindingResult.rejectValue("email", "error.email", 
                        "Este email já está registado");
                return "pages/auth/register";
            }

            User user = userService.registerUser(form.getName(), form.getEmail(), form.getPassword());
            
            transactionCategoryService.createDefaultCategories(user);
            
            log.info("User registered successfully: {}", user.getEmail());
            redirectAttributes.addFlashAttribute("message", 
                    "Registo realizado com sucesso! Pode fazer login.");
            
            return "redirect:/login";
            
        } catch (Exception e) {
            log.error("Error during user registration", e);
            model.addAttribute("error", "Ocorreu um erro durante o registo. Tente novamente.");
            return "pages/auth/register";
        }
    }

    @PostMapping("/logout")
    public String logout() {
        // This endpoint won't actually be called since Spring Security handles logout
        // but having it here provides a fallback and cleaner URL mapping
        return "redirect:/login?logout=true";
    }

    @RequestMapping("/logout")
    public String logoutGet() {
        // Handle GET requests to /logout by redirecting to login with logout message
        return "redirect:/login?logout=true";
    }

    @Data
    public static class RegistrationForm {
        
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        private String name;
        
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Formato de email inválido")
        private String email;
        
        @NotBlank(message = "A password é obrigatória")
        @Size(min = 6, message = "A password deve ter pelo menos 6 caracteres")
        private String password;
        
        @NotBlank(message = "A confirmação da password é obrigatória")
        private String confirmPassword;
    }
}