package com.example.myfinances.service;

import com.example.myfinances.model.TransactionCategory;
import com.example.myfinances.model.User;
import com.example.myfinances.repository.TransactionCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionCategoryService {

    private final TransactionCategoryRepository transactionCategoryRepository;

    public TransactionCategory createCategory(User user, String name, TransactionCategory.CategoryType type, String color, String icon, TransactionCategory parent) {
        log.info("Creating category '{}' for user ID: {}", name, user.getId());
        
        TransactionCategory category = TransactionCategory.builder()
                .user(user)
                .name(name)
                .type(type)
                .color(color != null ? color : "#6B7280")
                .icon(icon != null ? icon : "fas fa-circle")
                .parent(parent)
                .build();

        TransactionCategory savedCategory = transactionCategoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return savedCategory;
    }

    @Transactional(readOnly = true)
    public List<TransactionCategory> findCategoriesByUser(User user) {
        return transactionCategoryRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<TransactionCategory> findCategoriesByUserAndType(User user, TransactionCategory.CategoryType type) {
        return transactionCategoryRepository.findByUserAndType(user, type);
    }

    @Transactional(readOnly = true)
    public List<TransactionCategory> findParentCategories(User user) {
        return transactionCategoryRepository.findParentCategories(user);
    }

    @Transactional(readOnly = true)
    public List<TransactionCategory> findSubcategories(User user, TransactionCategory parent) {
        return transactionCategoryRepository.findSubcategories(user, parent);
    }

    @Transactional(readOnly = true)
    public Optional<TransactionCategory> findByIdAndUser(Long categoryId, User user) {
        return transactionCategoryRepository.findById(categoryId)
                .filter(category -> category.getUser().equals(user));
    }

    @Transactional(readOnly = true)
    public Optional<TransactionCategory> findCategoryByIdAndUser(Long categoryId, User user) {
        return findByIdAndUser(categoryId, user);
    }

    public TransactionCategory updateCategory(TransactionCategory category) {
        log.info("Updating category ID: {}", category.getId());
        return transactionCategoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId, User user) {
        TransactionCategory category = findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or not owned by user"));
        
        List<TransactionCategory> subcategories = findSubcategories(user, category);
        if (!subcategories.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories. Delete subcategories first.");
        }
        
        transactionCategoryRepository.delete(category);
        log.info("Category deleted: {}", categoryId);
    }

    public void createDefaultCategories(User user) {
        log.info("Creating default categories for user ID: {}", user.getId());
        
        createIncomeCategories(user);
        createExpenseCategories(user);
        
        log.info("Default categories created for user ID: {}", user.getId());
    }

    private void createIncomeCategories(User user) {
        TransactionCategory income = createCategory(user, "Receitas", TransactionCategory.CategoryType.INCOME, "#10B981", "fas fa-plus-circle", null);
        createCategory(user, "Salário", TransactionCategory.CategoryType.INCOME, "#059669", "fas fa-briefcase", income);
        createCategory(user, "Freelance", TransactionCategory.CategoryType.INCOME, "#047857", "fas fa-laptop-code", income);
        createCategory(user, "Investimentos", TransactionCategory.CategoryType.INCOME, "#065F46", "fas fa-chart-line", income);
        createCategory(user, "Outros", TransactionCategory.CategoryType.INCOME, "#064E3B", "fas fa-hand-holding-usd", income);
    }

    private void createExpenseCategories(User user) {
        TransactionCategory expenses = createCategory(user, "Despesas", TransactionCategory.CategoryType.EXPENSE, "#EF4444", "fas fa-minus-circle", null);
        
        TransactionCategory housing = createCategory(user, "Habitação", TransactionCategory.CategoryType.EXPENSE, "#DC2626", "fas fa-home", expenses);
        createCategory(user, "Renda/Prestação", TransactionCategory.CategoryType.EXPENSE, "#B91C1C", "fas fa-house-user", housing);
        createCategory(user, "Condomínio", TransactionCategory.CategoryType.EXPENSE, "#991B1B", "fas fa-building", housing);
        createCategory(user, "Utilities", TransactionCategory.CategoryType.EXPENSE, "#7F1D1D", "fas fa-bolt", housing);
        
        TransactionCategory food = createCategory(user, "Alimentação", TransactionCategory.CategoryType.EXPENSE, "#F59E0B", "fas fa-utensils", expenses);
        createCategory(user, "Supermercado", TransactionCategory.CategoryType.EXPENSE, "#D97706", "fas fa-shopping-cart", food);
        createCategory(user, "Restaurantes", TransactionCategory.CategoryType.EXPENSE, "#B45309", "fas fa-hamburger", food);
        
        TransactionCategory transport = createCategory(user, "Transportes", TransactionCategory.CategoryType.EXPENSE, "#3B82F6", "fas fa-car", expenses);
        createCategory(user, "Combustível", TransactionCategory.CategoryType.EXPENSE, "#2563EB", "fas fa-gas-pump", transport);
        createCategory(user, "Transportes Públicos", TransactionCategory.CategoryType.EXPENSE, "#1D4ED8", "fas fa-bus", transport);
        createCategory(user, "Manutenção", TransactionCategory.CategoryType.EXPENSE, "#1E40AF", "fas fa-wrench", transport);
        
        TransactionCategory health = createCategory(user, "Saúde", TransactionCategory.CategoryType.EXPENSE, "#06B6D4", "fas fa-heartbeat", expenses);
        createCategory(user, "Médico", TransactionCategory.CategoryType.EXPENSE, "#0891B2", "fas fa-user-md", health);
        createCategory(user, "Medicamentos", TransactionCategory.CategoryType.EXPENSE, "#0E7490", "fas fa-pills", health);
        createCategory(user, "Seguro Saúde", TransactionCategory.CategoryType.EXPENSE, "#155E75", "fas fa-shield-alt", health);
        
        TransactionCategory entertainment = createCategory(user, "Entretenimento", TransactionCategory.CategoryType.EXPENSE, "#8B5CF6", "fas fa-gamepad", expenses);
        createCategory(user, "Cinema/Teatro", TransactionCategory.CategoryType.EXPENSE, "#7C3AED", "fas fa-film", entertainment);
        createCategory(user, "Streaming", TransactionCategory.CategoryType.EXPENSE, "#6D28D9", "fas fa-play", entertainment);
        createCategory(user, "Hobbies", TransactionCategory.CategoryType.EXPENSE, "#5B21B6", "fas fa-palette", entertainment);
        
        createCategory(user, "Compras", TransactionCategory.CategoryType.EXPENSE, "#EC4899", "fas fa-shopping-bag", expenses);
        createCategory(user, "Educação", TransactionCategory.CategoryType.EXPENSE, "#F97316", "fas fa-graduation-cap", expenses);
        createCategory(user, "Seguros", TransactionCategory.CategoryType.EXPENSE, "#84CC16", "fas fa-umbrella", expenses);
        createCategory(user, "Impostos", TransactionCategory.CategoryType.EXPENSE, "#64748B", "fas fa-file-invoice-dollar", expenses);
        createCategory(user, "Outros", TransactionCategory.CategoryType.EXPENSE, "#6B7280", "fas fa-question-circle", expenses);
    }
}