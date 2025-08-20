package com.example.myfinances.controller.web;

import com.example.myfinances.model.Investment;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.model.UserInvestmentProduct;
import com.example.myfinances.security.SecurityUtils;
import com.example.myfinances.service.InvestmentProductService;
import com.example.myfinances.service.InvestmentService;
import com.example.myfinances.service.UserInvestmentProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.http.ResponseEntity;

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
    private final UserInvestmentProductService userInvestmentProductService;

    @GetMapping
    public String portfolio(Authentication authentication, Model model, HttpServletRequest request,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "type", required = false) InvestmentProduct.InvestmentType type) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        List<Investment> investments;
        if (search != null && !search.trim().isEmpty()) {
            investments = investmentService.searchInvestments(user, search);
        } else if (type != null) {
            investments = investmentService.findByUserAndType(user, type);
        } else {
            investments = investmentService.findByUser(user);
        }
        
        InvestmentService.PortfolioSummary portfolio = investmentService.getPortfolioSummary(user);
        List<InvestmentService.TypeAllocation> allocations = investmentService.getPortfolioAllocationByType(user);
        List<InvestmentService.CurrencyAllocation> currencyAllocations = investmentService.getPortfolioAllocationByCurrency(user);
        
        model.addAttribute("investments", investments);
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("typeAllocations", allocations);
        model.addAttribute("currencyAllocations", currencyAllocations);
        model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("searchTerm", search);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/portfolio";
    }

    @GetMapping("/products")
    public String products(Authentication authentication, Model model, HttpServletRequest request,
                          @RequestParam(value = "search", required = false) String search,
                          @RequestParam(value = "type", required = false) InvestmentProduct.InvestmentType type,
                          @RequestParam(value = "favorites", required = false) Boolean showFavorites) {
        
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        List<UserInvestmentProduct> userProducts;
        
        if (Boolean.TRUE.equals(showFavorites)) {
            // Show only favorites
            userProducts = userInvestmentProductService.getUserFavoriteProducts(user);
        } else if (search != null && !search.trim().isEmpty()) {
            // Search in user's saved products
            userProducts = userInvestmentProductService.searchUserSavedProducts(user, search);
        } else if (type != null) {
            // Filter by type in user's saved products
            userProducts = userInvestmentProductService.getUserSavedProductsByType(user, type);
        } else {
            // Show all user's saved products
            userProducts = userInvestmentProductService.getUserSavedProducts(user);
        }
        
        long savedProductsCount = userInvestmentProductService.getSavedProductsCount(user);
        
        model.addAttribute("userProducts", userProducts);
        model.addAttribute("savedProductsCount", savedProductsCount);
        model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("showFavorites", showFavorites);
        model.addAttribute("searchTerm", search);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/products";
    }

    @GetMapping("/products/search-api")
    public String searchApiForm(Model model, HttpServletRequest request) {
        model.addAttribute("searchForm", new ApiSearchForm());
        model.addAttribute("currentPath", request.getRequestURI());
        return "pages/investments/search-api";
    }

    @PostMapping("/products/search-api")
    public String searchApi(@Valid @ModelAttribute("searchForm") ApiSearchForm form,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPath", "/investments/products/search-api");
            return "pages/investments/search-api";
        }

        try {
            List<String> symbols = List.of(form.getSymbols().split("[,\\s]+"));
            symbols = symbols.stream()
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .filter(s -> !s.isEmpty())
                    .toList();

            List<InvestmentProduct> products = investmentProductService.searchProductsFromApi(
                    symbols, form.getRegion(), true);

            // If user wants to save to their profile, save the found products
            User user = SecurityUtils.getCurrentUserOrThrow(authentication);
            if (form.isSaveToProfile() && !products.isEmpty()) {
                for (InvestmentProduct product : products) {
                    // Product should already be saved to database by searchProductsFromApi
                    // Save directly to user profile without checking if it exists
                    // (the service method will handle duplicates)
                    try {
                        userInvestmentProductService.saveProductToUser(user, product);
                        log.debug("Saved product {} to user {} profile", product.getSymbol(), user.getEmail());
                    } catch (Exception e) {
                        log.warn("Could not save product {} to user profile: {}", product.getSymbol(), e.getMessage());
                    }
                }
            }

            model.addAttribute("products", products);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("symbolsSearched", symbols);
            model.addAttribute("currentPath", "/investments/products/search-api");

            if (products.isEmpty()) {
                model.addAttribute("warning", "No products found for the specified symbols.");
            } else {
                String message = String.format("Found %d product(s) for symbols: %s", 
                        products.size(), String.join(", ", symbols));
                if (form.isSaveToProfile()) {
                    message += " (saved to your profile)";
                }
                model.addAttribute("success", message);
            }

            return "pages/investments/search-api";

        } catch (Exception e) {
            log.error("Error searching products from API", e);
            model.addAttribute("error", "Error searching products: " + e.getMessage());
            model.addAttribute("currentPath", "/investments/products/search-api");
            return "pages/investments/search-api";
        }
    }

    @GetMapping("/add")
    public String addInvestmentForm(Authentication authentication, Model model, HttpServletRequest request,
                                   @RequestParam(value = "productId", required = false) Long productId) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        InvestmentForm form = new InvestmentForm();
        if (productId != null) {
            form.setProductId(productId);
        }
        
        List<InvestmentProduct> products = userInvestmentProductService.getUserSavedProductsList(user);
        
        model.addAttribute("investmentForm", form);
        model.addAttribute("products", products);
        model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/add";
    }

    @PostMapping("/add")
    public String addInvestment(@Valid @ModelAttribute("investmentForm") InvestmentForm form,
                               BindingResult bindingResult,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        if (bindingResult.hasErrors()) {
            List<InvestmentProduct> products = userInvestmentProductService.getUserSavedProductsList(user);
            model.addAttribute("products", products);
            model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
            return "pages/investments/add";
        }

        try {
            Optional<InvestmentProduct> productOpt = investmentProductService.findById(form.getProductId());
            if (productOpt.isEmpty()) {
                model.addAttribute("error", "Investment product not found.");
                List<InvestmentProduct> products = userInvestmentProductService.getUserSavedProductsList(user);
                model.addAttribute("products", products);
                model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
                return "pages/investments/add";
            }
            
            InvestmentProduct product = productOpt.get();
            
            Investment investment = investmentService.createInvestment(
                user,
                product,
                form.getQuantity(),
                form.getPurchasePrice(),
                form.getPurchaseDate(),
                form.getExchangeRate(),
                form.getNotes()
            );
            
            log.info("Investment created successfully for user {}: {}", user.getId(), investment.getId());
            redirectAttributes.addFlashAttribute("success", 
                    "Investment in " + product.getName() + " added successfully!");
            
            return "redirect:/investments";
            
        } catch (Exception e) {
            log.error("Error creating investment", e);
            model.addAttribute("error", "An error occurred while creating the investment: " + e.getMessage());
            List<InvestmentProduct> products = userInvestmentProductService.getUserSavedProductsList(user);
            model.addAttribute("products", products);
            model.addAttribute("investmentTypes", InvestmentProduct.InvestmentType.values());
            return "pages/investments/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editInvestmentForm(@PathVariable Long id, Authentication authentication, 
                                   Model model, HttpServletRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        Optional<Investment> investment = investmentService.findByIdAndUser(id, user);
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
        
        List<InvestmentProduct> products = userInvestmentProductService.getUserSavedProductsList(user);
        
        model.addAttribute("investmentForm", form);
        model.addAttribute("investment", inv);
        model.addAttribute("products", products);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/edit";
    }

    @PostMapping("/edit/{id}")
    public String editInvestment(@PathVariable Long id,
                                @Valid @ModelAttribute("investmentForm") InvestmentForm form,
                                BindingResult bindingResult,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        Optional<Investment> existingInvestment = investmentService.findByIdAndUser(id, user);
        if (existingInvestment.isEmpty()) {
            return "redirect:/investments";
        }
        
        if (bindingResult.hasErrors()) {
            List<InvestmentProduct> products = userInvestmentProductService.getUserSavedProductsList(user);
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
            redirectAttributes.addFlashAttribute("success", 
                    "Investment updated successfully!");
            
            return "redirect:/investments";
            
        } catch (Exception e) {
            log.error("Error updating investment", e);
            redirectAttributes.addFlashAttribute("error", 
                    "An error occurred while updating the investment.");
            return "redirect:/investments/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteInvestment(@PathVariable Long id, Authentication authentication, 
                                  RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        Optional<Investment> investment = investmentService.findByIdAndUser(id, user);
        if (investment.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Investment not found.");
            return "redirect:/investments";
        }
        
        try {
            String investmentName = investment.get().getProduct().getName();
            investmentService.deleteInvestment(investment.get());
            
            log.info("Investment deleted successfully: {}", id);
            redirectAttributes.addFlashAttribute("success", 
                    "Investment in " + investmentName + " deleted successfully!");
            
        } catch (Exception e) {
            log.error("Error deleting investment", e);
            redirectAttributes.addFlashAttribute("error", 
                    "An error occurred while deleting the investment.");
        }
        
        return "redirect:/investments";
    }

    @PostMapping("/update-prices")
    public String updatePrices(Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        try {
            investmentService.updateInvestmentPrices(user);
            redirectAttributes.addFlashAttribute("success", "Prices updated successfully!");
        } catch (Exception e) {
            log.error("Error updating prices", e);
            redirectAttributes.addFlashAttribute("error", "Error updating prices: " + e.getMessage());
        }
        
        return "redirect:/investments";
    }

    // Product management endpoints
    
    @PostMapping("/products/save/{productId}")
    public String saveProduct(@PathVariable Long productId, 
                             Authentication authentication,
                             @RequestParam(value = "notes", required = false) String notes,
                             @RequestParam(value = "favorite", required = false) Boolean isFavorite,
                             RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        try {
            Optional<InvestmentProduct> productOpt = investmentProductService.findById(productId);
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/investments/products";
            }
            
            InvestmentProduct product = productOpt.get();
            userInvestmentProductService.saveProductToUser(user, product, notes, isFavorite);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Product " + product.getSymbol() + " saved to your profile!");
            
        } catch (Exception e) {
            log.error("Error saving product to user profile", e);
            redirectAttributes.addFlashAttribute("error", "Error saving product: " + e.getMessage());
        }
        
        return "redirect:/investments/products";
    }
    
    @PostMapping("/products/remove/{productId}")
    public String removeProduct(@PathVariable Long productId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        try {
            Optional<InvestmentProduct> productOpt = investmentProductService.findById(productId);
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/investments/products";
            }
            
            InvestmentProduct product = productOpt.get();
            userInvestmentProductService.removeProductFromUser(user, product);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Product " + product.getSymbol() + " removed from your profile!");
            
        } catch (Exception e) {
            log.error("Error removing product from user profile", e);
            redirectAttributes.addFlashAttribute("error", "Error removing product: " + e.getMessage());
        }
        
        return "redirect:/investments/products";
    }
    
    @PostMapping("/products/toggle-favorite/{productId}")
    public String toggleFavorite(@PathVariable Long productId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUserOrThrow(authentication);
        
        try {
            Optional<InvestmentProduct> productOpt = investmentProductService.findById(productId);
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/investments/products";
            }
            
            InvestmentProduct product = productOpt.get();
            UserInvestmentProduct userProduct = userInvestmentProductService.toggleFavorite(user, product);
            
            String message = userProduct.getIsFavorite() 
                    ? "Added " + product.getSymbol() + " to favorites!"
                    : "Removed " + product.getSymbol() + " from favorites!";
            redirectAttributes.addFlashAttribute("success", message);
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Product not found in your saved list.");
        } catch (Exception e) {
            log.error("Error toggling favorite status", e);
            redirectAttributes.addFlashAttribute("error", "Error updating favorite status: " + e.getMessage());
        }
        
        return "redirect:/investments/products";
    }

    // AJAX endpoints for smart product search
    @GetMapping("/api/products/search")
    @ResponseBody
    public ResponseEntity<List<InvestmentProduct>> searchProductsAjax(
            @RequestParam("q") String searchTerm,
            @RequestParam(value = "region", defaultValue = "US") String region) {
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        
        try {
            List<InvestmentProduct> products = investmentProductService.searchProductsSmart(searchTerm, region);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error in AJAX product search", e);
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/api/products/by-symbol")
    @ResponseBody
    public ResponseEntity<InvestmentProduct> getProductBySymbolAjax(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "currency", defaultValue = "USD") String currency,
            @RequestParam(value = "region", defaultValue = "US") String region) {
        
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            Optional<InvestmentProduct> product = investmentProductService
                    .searchProductBySymbolSmart(symbol, currency, region);
            
            if (product.isPresent()) {
                return ResponseEntity.ok(product.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error in AJAX product search by symbol", e);
            return ResponseEntity.notFound().build();
        }
    }

    // Form classes
    @Data
    public static class InvestmentForm {
        @NotNull(message = "Product selection is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.000001", message = "Quantity must be greater than 0")
        private BigDecimal quantity;
        
        @NotNull(message = "Purchase price is required")
        @DecimalMin(value = "0.01", message = "Purchase price must be greater than 0")
        private BigDecimal purchasePrice;
        
        @NotNull(message = "Purchase date is required")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate purchaseDate;
        
        @DecimalMin(value = "0.000001", message = "Exchange rate must be greater than 0")
        private BigDecimal exchangeRate = BigDecimal.ONE;
        
        private String notes;
    }

    @Data
    public static class ApiSearchForm {
        @NotNull(message = "Symbols are required")
        private String symbols;
        
        private String region = "US";
        
        private boolean saveToProfile = true;
    }
}