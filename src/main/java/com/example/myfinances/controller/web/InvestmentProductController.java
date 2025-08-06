package com.example.myfinances.controller.web;

import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.service.InvestmentProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/investments/products")
@RequiredArgsConstructor
@Slf4j
public class InvestmentProductController {

    private final InvestmentProductService productService;

    @GetMapping
    public String listProducts(Authentication authentication, Model model, HttpServletRequest request,
                             @RequestParam(value = "search", required = false) String search,
                             @RequestParam(value = "type", required = false) InvestmentProduct.InvestmentType type,
                             @RequestParam(value = "currency", required = false) String currency) {
        User user = (User) authentication.getPrincipal();
        
        List<InvestmentProduct> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchActiveProducts(user, search);
        } else if (type != null) {
            products = productService.findActiveProductsByUserAndType(user, type);
        } else if (currency != null) {
            products = productService.findActiveProductsByUserAndCurrency(user, currency);
        } else {
            products = productService.findActiveProductsByUser(user);
        }
        
        InvestmentProductService.ProductSummary summary = productService.getProductSummary(user);
        
        model.addAttribute("products", products);
        model.addAttribute("summary", summary);
        model.addAttribute("productTypes", InvestmentProduct.InvestmentType.values());
        model.addAttribute("currencies", InvestmentProduct.Currency.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCurrency", currency);
        model.addAttribute("searchTerm", search);
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/products/list";
    }

    @GetMapping("/add")
    public String addProductForm(Model model, HttpServletRequest request) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("productTypes", InvestmentProduct.InvestmentType.values());
        model.addAttribute("currencies", InvestmentProduct.Currency.values());
        model.addAttribute("currentPath", request.getRequestURI());
        return "pages/investments/products/add";
    }

    @PostMapping("/add")
    public String addProduct(
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) authentication.getPrincipal();
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("productTypes", InvestmentProduct.InvestmentType.values());
            model.addAttribute("currencies", InvestmentProduct.Currency.values());
            return "pages/investments/products/add";
        }

        // Check if product already exists
        if (productService.productExists(user, form.getSymbol(), form.getCurrency())) {
            bindingResult.rejectValue("symbol", "error.symbol", 
                    "Produto já existe com este símbolo e moeda");
            
            model.addAttribute("productTypes", InvestmentProduct.InvestmentType.values());
            model.addAttribute("currencies", InvestmentProduct.Currency.values());
            return "pages/investments/products/add";
        }

        try {
            InvestmentProduct product = productService.createProduct(
                user,
                form.getSymbol(),
                form.getName(),
                form.getDescription(),
                form.getType(),
                form.getCurrency()
            );
            
            // Set additional fields
            if (form.getExchange() != null && !form.getExchange().trim().isEmpty()) {
                product.setExchange(form.getExchange().trim());
            }
            if (form.getSector() != null && !form.getSector().trim().isEmpty()) {
                product.setSector(form.getSector().trim());
            }
            if (form.getRegion() != null && !form.getRegion().trim().isEmpty()) {
                product.setRegion(form.getRegion().trim());
            }
            if (form.getCurrentPrice() != null) {
                product.setCurrentPrice(form.getCurrentPrice());
            }
            
            productService.updateProduct(product);
            
            log.info("Investment product created successfully: {}", product.getName());
            redirectAttributes.addFlashAttribute("message", 
                    "Produto '" + product.getName() + "' criado com sucesso!");
            
            return "redirect:/investments/products";
            
        } catch (Exception e) {
            log.error("Error creating investment product", e);
            model.addAttribute("error", "Ocorreu um erro ao criar o produto. Tente novamente.");
            model.addAttribute("productTypes", InvestmentProduct.InvestmentType.values());
            model.addAttribute("currencies", InvestmentProduct.Currency.values());
            return "pages/investments/products/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Authentication authentication, 
                                Model model, HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        
        Optional<InvestmentProduct> product = productService.findProductByIdAndUser(id, user);
        if (product.isEmpty()) {
            return "redirect:/investments/products";
        }
        
        ProductForm form = new ProductForm();
        InvestmentProduct prod = product.get();
        form.setSymbol(prod.getSymbol());
        form.setName(prod.getName());
        form.setDescription(prod.getDescription());
        form.setType(prod.getType());
        form.setCurrency(prod.getCurrency());
        form.setExchange(prod.getExchange());
        form.setSector(prod.getSector());
        form.setRegion(prod.getRegion());
        form.setCurrentPrice(prod.getCurrentPrice());
        
        model.addAttribute("productForm", form);
        model.addAttribute("product", prod);
        model.addAttribute("productTypes", InvestmentProduct.InvestmentType.values());
        model.addAttribute("currencies", InvestmentProduct.Currency.values());
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/investments/products/edit";
    }

    @PostMapping("/edit/{id}")
    public String editProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) authentication.getPrincipal();
        
        Optional<InvestmentProduct> existingProduct = productService.findProductByIdAndUser(id, user);
        if (existingProduct.isEmpty()) {
            return "redirect:/investments/products";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", existingProduct.get());
            model.addAttribute("productTypes", InvestmentProduct.InvestmentType.values());
            model.addAttribute("currencies", InvestmentProduct.Currency.values());
            return "pages/investments/products/edit";
        }

        try {
            InvestmentProduct product = existingProduct.get();
            
            // Check if symbol+currency combination already exists (excluding current product)
            if (!product.getSymbol().equals(form.getSymbol().toUpperCase()) || 
                !product.getCurrency().equals(form.getCurrency().toUpperCase())) {
                if (productService.productExists(user, form.getSymbol(), form.getCurrency())) {
                    bindingResult.rejectValue("symbol", "error.symbol", 
                            "Produto já existe com este símbolo e moeda");
                    
                    model.addAttribute("product", product);
                    model.addAttribute("productTypes", InvestmentProduct.InvestmentType.values());
                    model.addAttribute("currencies", InvestmentProduct.Currency.values());
                    return "pages/investments/products/edit";
                }
            }
            
            product.setSymbol(form.getSymbol().toUpperCase());
            product.setName(form.getName());
            product.setDescription(form.getDescription());
            product.setType(form.getType());
            product.setCurrency(form.getCurrency().toUpperCase());
            product.setExchange(form.getExchange());
            product.setSector(form.getSector());
            product.setRegion(form.getRegion());
            product.setCurrentPrice(form.getCurrentPrice());
            
            productService.updateProduct(product);
            
            log.info("Investment product updated successfully: {}", product.getId());
            redirectAttributes.addFlashAttribute("message", 
                    "Produto '" + product.getName() + "' atualizado com sucesso!");
            
            return "redirect:/investments/products";
            
        } catch (Exception e) {
            log.error("Error updating investment product", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao atualizar o produto. Tente novamente.");
            return "redirect:/investments/products/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        User user = (User) authentication.getPrincipal();
        
        Optional<InvestmentProduct> product = productService.findProductByIdAndUser(id, user);
        if (product.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Produto não encontrado.");
            return "redirect:/investments/products";
        }
        
        try {
            String productName = product.get().getName();
            productService.deleteProduct(product.get());
            
            log.info("Investment product deleted successfully: {}", id);
            redirectAttributes.addFlashAttribute("message", 
                    "Produto '" + productName + "' desativado com sucesso!");
            
        } catch (Exception e) {
            log.error("Error deleting investment product", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao desativar o produto. Tente novamente.");
        }
        
        return "redirect:/investments/products";
    }

    @PostMapping("/update-price/{id}")
    public String updatePrice(@PathVariable Long id,
                            @RequestParam("currentPrice") BigDecimal currentPrice,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        User user = (User) authentication.getPrincipal();
        
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) < 0) {
            redirectAttributes.addFlashAttribute("error", "Preço inválido.");
            return "redirect:/investments/products";
        }
        
        try {
            InvestmentProduct product = productService.updateProductPrice(id, currentPrice, user);
            
            log.info("Price updated for product: {} to {}", id, currentPrice);
            redirectAttributes.addFlashAttribute("message", 
                    "Preço atualizado para " + product.getFormattedPrice());
            
        } catch (Exception e) {
            log.error("Error updating price", e);
            redirectAttributes.addFlashAttribute("error", 
                    "Ocorreu um erro ao atualizar o preço. Tente novamente.");
        }
        
        return "redirect:/investments/products";
    }

    @Data
    public static class ProductForm {
        @NotBlank(message = "O símbolo é obrigatório")
        @Size(min = 1, max = 20, message = "O símbolo deve ter entre 1 e 20 caracteres")
        private String symbol;
        
        @NotBlank(message = "O nome do produto é obrigatório")
        @Size(min = 2, max = 200, message = "O nome deve ter entre 2 e 200 caracteres")
        private String name;
        
        @Size(max = 500, message = "A descrição não pode ter mais de 500 caracteres")
        private String description;
        
        @NotNull(message = "O tipo de produto é obrigatório")
        private InvestmentProduct.InvestmentType type;
        
        @NotBlank(message = "A moeda é obrigatória")
        private String currency;
        
        @Size(max = 100, message = "A exchange não pode ter mais de 100 caracteres")
        private String exchange;
        
        @Size(max = 50, message = "O setor não pode ter mais de 50 caracteres")
        private String sector;
        
        @Size(max = 50, message = "A região não pode ter mais de 50 caracteres")
        private String region;
        
        private BigDecimal currentPrice;
    }
}