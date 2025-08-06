package com.example.myfinances.controller.web;

import com.example.myfinances.model.Investment;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.service.InvestmentService;
import com.example.myfinances.service.InvestmentProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/investments")
@RequiredArgsConstructor
@Slf4j
public class InvestmentController {

    private final InvestmentService investmentService;
    private final InvestmentProductService investmentProductService;

    @GetMapping
    public String portfolio(Authentication authentication, Model model, HttpServletRequest request,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "type", required = false) InvestmentProduct.InvestmentType type) {
        User user = (User) authentication.getPrincipal();
        
        List<Investment> investments;
        if (search != null && !search.trim().isEmpty()) {
            investments = investmentService.searchInvestments(user, search);
        } else if (type != null) {
            investments = investmentService.findInvestmentsByUserAndProductType(user, type);
        } else {
            investments = investmentService.findInvestmentsByUser(user);
        }
        
        InvestmentService.PortfolioSummary portfolio = investmentService.getPortfolioSummary(user);
        List<InvestmentService.TypeAllocation> allocations = investmentService.getPortfolioAllocationByType(user);
        
        model.addAttribute("investments", investments);
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("allocations", allocations);
        model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("searchTerm", search);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/portfolio";
    }

    @GetMapping("/add")
    public String addInvestmentForm(Authentication authentication, Model model, HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        List<InvestmentProduct> products = investmentProductService.findActiveProductsByUser(user);
        model.addAttribute("investmentForm", new InvestmentForm());
        model.addAttribute("products", products);
        model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
        model.addAttribute("currentPath", request.getRequestURI());
        return "pages/investments/add";
    }

    @PostMapping("/add")
    public String addInvestment(
            @Valid @ModelAttribute("investmentForm") InvestmentForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) authentication.getPrincipal();
        
        if (bindingResult.hasErrors()) {
            List<InvestmentProduct> products = investmentProductService.findActiveProductsByUser(user);
            model.addAttribute("products", products);
            model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
            return "pages/investments/add";
        }

        try {
            InvestmentProduct product;
            
            if (form.isCreateNewProduct()) {
                // Create new product
                product = investmentProductService.createOrUpdateProduct(
                    user, 
                    form.getSymbol() != null ? form.getSymbol() : "UNKNOWN",
                    form.getName(),
                    form.getDescription(),
                    form.getType(),
                    form.getCurrency(),
                    null, // exchange
                    null, // sector
                    null  // region
                );
            } else {
                // Use existing product
                Optional<InvestmentProduct> productOpt = investmentProductService.findProductByIdAndUser(form.getProductId(), user);
                if (productOpt.isEmpty()) {
                    model.addAttribute("error", "Produto de investimento não encontrado.");
                    List<InvestmentProduct> products = investmentProductService.findActiveProductsByUser(user);
                    model.addAttribute("products", products);
                    model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
                    return "pages/investments/add";
                }
                product = productOpt.get();
            }
            
            Investment investment = investmentService.createInvestmentPosition(
                user,
                product,
                form.getQuantity(),
                form.getPurchasePrice(),
                form.getPurchaseDate(),
                form.getExchangeRate(),
                form.getNotes()
            );
            
            log.info("Investment created successfully: {}", investment.getProduct().getName());
            redirectAttributes.addFlashAttribute("message", 
                    "Investimento '" + investment.getProduct().getName() + "' adicionado com sucesso!");
            
            return "redirect:/investments";
            
        } catch (Exception e) {
            log.error("Error creating investment", e);
            model.addAttribute("error", "Ocorreu um erro ao criar o investimento. Tente novamente.");
            List<InvestmentProduct> products = investmentProductService.findActiveProductsByUser(user);
            model.addAttribute("products", products);
            model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
            return "pages/investments/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editInvestmentForm(@PathVariable Long id, Authentication authentication, 
                                   Model model, HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        
        Optional<Investment> investment = investmentService.findInvestmentByIdAndUser(id, user);
        if (investment.isEmpty()) {
            return "redirect:/investments";
        }
        
        InvestmentForm form = new InvestmentForm();
        Investment inv = investment.get();
        form.setProductId(inv.getProduct().getId());
        form.setQuantity(inv.getQuantity());
        form.setPurchasePrice(inv.getPurchasePrice());
        form.setPurchaseDate(inv.getPurchaseDate());
        form.setExchangeRate(inv.getExchangeRate());
        form.setNotes(inv.getNotes());
        
        List<InvestmentProduct> products = investmentProductService.findActiveProductsByUser(user);
        model.addAttribute("investmentForm", form);
        model.addAttribute("investment", inv);
        model.addAttribute("products", products);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/edit";
    }

    @PostMapping("/edit/{id}")
    public String editInvestment(
            @PathVariable Long id,
            @Valid @ModelAttribute("investmentForm") InvestmentForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) authentication.getPrincipal();
        
        Optional<Investment> existingInvestment = investmentService.findInvestmentByIdAndUser(id, user);
        if (existingInvestment.isEmpty()) {
            return "redirect:/investments";
        }
        
        if (bindingResult.hasErrors()) {
            List<InvestmentProduct> products = investmentProductService.findActiveProductsByUser(user);
            model.addAttribute("investment", existingInvestment.get());
            model.addAttribute("products", products);
            return "pages/investments/edit";
        }

        try {
            Investment investment = existingInvestment.get();
            investment.setQuantity(form.getQuantity());
            investment.setPurchasePrice(form.getPurchasePrice());
            investment.setPurchaseDate(form.getPurchaseDate());
            investment.setExchangeRate(form.getExchangeRate());
            investment.setNotes(form.getNotes());
            
            investmentService.updateInvestment(investment);
            
            log.info("Investment updated successfully: {}", investment.getId());
            redirectAttributes.addFlashAttribute("message", 
                    "Investimento '" + investment.getProduct().getName() + "' atualizado com sucesso!");
            
            return "redirect:/investments";
            
        } catch (Exception e) {
            log.error("Error updating investment", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao atualizar o investimento. Tente novamente.");
            return "redirect:/investments/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteInvestment(@PathVariable Long id, Authentication authentication, 
                                 RedirectAttributes redirectAttributes) {
        User user = (User) authentication.getPrincipal();
        
        Optional<Investment> investment = investmentService.findInvestmentByIdAndUser(id, user);
        if (investment.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Investimento não encontrado.");
            return "redirect:/investments";
        }
        
        try {
            String investmentName = investment.get().getProduct().getName();
            investmentService.deleteInvestment(investment.get());
            
            log.info("Investment deleted successfully: {}", id);
            redirectAttributes.addFlashAttribute("message", 
                    "Investimento '" + investmentName + "' removido com sucesso!");
            
        } catch (Exception e) {
            log.error("Error deleting investment", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao remover o investimento. Tente novamente.");
        }
        
        return "redirect:/investments";
    }

    @PostMapping("/update-price/{id}")
    public String updatePrice(@PathVariable Long id,
                            @RequestParam("currentPrice") BigDecimal currentPrice,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        User user = (User) authentication.getPrincipal();
        
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) < 0) {
            redirectAttributes.addFlashAttribute("error", "Preço inválido.");
            return "redirect:/investments";
        }
        
        try {
            Optional<Investment> investmentOpt = investmentService.findInvestmentByIdAndUser(id, user);
            if (investmentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Investimento não encontrado.");
                return "redirect:/investments";
            }
            
            Investment investment = investmentOpt.get();
            InvestmentProduct product = investment.getProduct();
            product.setCurrentPrice(currentPrice);
            investmentProductService.updateProduct(product);
            
            log.info("Price updated for product: {} to {}", product.getSymbol(), currentPrice);
            redirectAttributes.addFlashAttribute("message", 
                    "Preço atualizado para " + currentPrice + " " + product.getCurrency());
            
        } catch (Exception e) {
            log.error("Error updating price", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao atualizar o preço. Tente novamente.");
        }
        
        return "redirect:/investments";
    }

    @GetMapping("/details/{id}")
    public String investmentDetails(@PathVariable Long id, Authentication authentication, 
                                  Model model, HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        
        Optional<Investment> investment = investmentService.findInvestmentByIdAndUser(id, user);
        if (investment.isEmpty()) {
            return "redirect:/investments";
        }
        
        model.addAttribute("investment", investment.get());
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/details";
    }

    @Data
    public static class InvestmentForm {
        // For creating new products
        private InvestmentProduct.InvestmentType type;
        private String name;
        private String symbol;
        private String currency = "EUR";
        private String description;
        
        // For existing products
        private Long productId;
        
        // For investment positions
        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(value = "0.000001", message = "A quantidade deve ser superior a 0")
        private BigDecimal quantity;
        
        @NotNull(message = "O preço de compra é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço de compra deve ser superior a 0")
        private BigDecimal purchasePrice;
        
        @NotNull(message = "A data de compra é obrigatória")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate purchaseDate;
        
        @DecimalMin(value = "0.000001", message = "A taxa de câmbio deve ser superior a 0")
        private BigDecimal exchangeRate = BigDecimal.ONE;
        
        @Size(max = 500, message = "As notas não podem ter mais de 500 caracteres")
        private String notes;
        
        // Mode selector
        private String mode = "existing"; // "new" or "existing"
        
        public boolean isCreateNewProduct() {
            return "new".equals(mode);
        }
    }
}