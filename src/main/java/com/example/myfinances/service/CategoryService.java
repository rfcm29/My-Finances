package com.example.myfinances.service;

import com.example.myfinances.model.Category;
import com.example.myfinances.model.User;
import com.example.myfinances.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category createCategory(User user, String name, Category.CategoryType type, String color, String icon, Category parent) {
        log.info("Creating category '{}' for user ID: {}", name, user.getId());
        
        Category category = Category.builder()
                .user(user)
                .name(name)
                .type(type)
                .color(color != null ? color : "#6B7280")
                .icon(icon != null ? icon : "fas fa-circle")
                .parent(parent)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return savedCategory;
    }

    @Transactional(readOnly = true)
    public List<Category> findCategoriesByUser(User user) {
        return categoryRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Category> findCategoriesByUserAndType(User user, Category.CategoryType type) {
        return categoryRepository.findByUserAndType(user, type);
    }

    @Transactional(readOnly = true)
    public List<Category> findParentCategories(User user) {
        return categoryRepository.findParentCategories(user);
    }

    @Transactional(readOnly = true)
    public List<Category> findSubcategories(User user, Category parent) {
        return categoryRepository.findSubcategories(user, parent);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByIdAndUser(Long categoryId, User user) {
        return categoryRepository.findById(categoryId)
                .filter(category -> category.getUser().equals(user));
    }

    @Transactional(readOnly = true)
    public Optional<Category> findCategoryByIdAndUser(Long categoryId, User user) {
        return findByIdAndUser(categoryId, user);
    }

    public Category updateCategory(Category category) {
        log.info("Updating category ID: {}", category.getId());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId, User user) {
        Category category = findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or not owned by user"));
        
        List<Category> subcategories = findSubcategories(user, category);
        if (!subcategories.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories. Delete subcategories first.");
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted: {}", categoryId);
    }

    public void createDefaultCategories(User user) {
        log.info("Creating default categories for user ID: {}", user.getId());
        
        createIncomeCategories(user);
        createExpenseCategories(user);
        
        log.info("Default categories created for user ID: {}", user.getId());
    }

    private void createIncomeCategories(User user) {
        Category income = createCategory(user, "Receitas", Category.CategoryType.INCOME, "#10B981", "fas fa-plus-circle", null);
        createCategory(user, "Salário", Category.CategoryType.INCOME, "#059669", "fas fa-briefcase", income);
        createCategory(user, "Freelance", Category.CategoryType.INCOME, "#047857", "fas fa-laptop-code", income);
        createCategory(user, "Investimentos", Category.CategoryType.INCOME, "#065F46", "fas fa-chart-line", income);
        createCategory(user, "Outros", Category.CategoryType.INCOME, "#064E3B", "fas fa-hand-holding-usd", income);
    }

    private void createExpenseCategories(User user) {
        Category expenses = createCategory(user, "Despesas", Category.CategoryType.EXPENSE, "#EF4444", "fas fa-minus-circle", null);
        
        Category housing = createCategory(user, "Habitação", Category.CategoryType.EXPENSE, "#DC2626", "fas fa-home", expenses);
        createCategory(user, "Renda/Prestação", Category.CategoryType.EXPENSE, "#B91C1C", "fas fa-house-user", housing);
        createCategory(user, "Condomínio", Category.CategoryType.EXPENSE, "#991B1B", "fas fa-building", housing);
        createCategory(user, "Utilities", Category.CategoryType.EXPENSE, "#7F1D1D", "fas fa-bolt", housing);
        
        Category food = createCategory(user, "Alimentação", Category.CategoryType.EXPENSE, "#F59E0B", "fas fa-utensils", expenses);
        createCategory(user, "Supermercado", Category.CategoryType.EXPENSE, "#D97706", "fas fa-shopping-cart", food);
        createCategory(user, "Restaurantes", Category.CategoryType.EXPENSE, "#B45309", "fas fa-hamburger", food);
        
        Category transport = createCategory(user, "Transportes", Category.CategoryType.EXPENSE, "#3B82F6", "fas fa-car", expenses);
        createCategory(user, "Combustível", Category.CategoryType.EXPENSE, "#2563EB", "fas fa-gas-pump", transport);
        createCategory(user, "Transportes Públicos", Category.CategoryType.EXPENSE, "#1D4ED8", "fas fa-bus", transport);
        createCategory(user, "Manutenção", Category.CategoryType.EXPENSE, "#1E40AF", "fas fa-wrench", transport);
        
        Category health = createCategory(user, "Saúde", Category.CategoryType.EXPENSE, "#06B6D4", "fas fa-heartbeat", expenses);
        createCategory(user, "Médico", Category.CategoryType.EXPENSE, "#0891B2", "fas fa-user-md", health);
        createCategory(user, "Medicamentos", Category.CategoryType.EXPENSE, "#0E7490", "fas fa-pills", health);
        createCategory(user, "Seguro Saúde", Category.CategoryType.EXPENSE, "#155E75", "fas fa-shield-alt", health);
        
        Category entertainment = createCategory(user, "Entretenimento", Category.CategoryType.EXPENSE, "#8B5CF6", "fas fa-gamepad", expenses);
        createCategory(user, "Cinema/Teatro", Category.CategoryType.EXPENSE, "#7C3AED", "fas fa-film", entertainment);
        createCategory(user, "Streaming", Category.CategoryType.EXPENSE, "#6D28D9", "fas fa-play", entertainment);
        createCategory(user, "Hobbies", Category.CategoryType.EXPENSE, "#5B21B6", "fas fa-palette", entertainment);
        
        createCategory(user, "Compras", Category.CategoryType.EXPENSE, "#EC4899", "fas fa-shopping-bag", expenses);
        createCategory(user, "Educação", Category.CategoryType.EXPENSE, "#F97316", "fas fa-graduation-cap", expenses);
        createCategory(user, "Seguros", Category.CategoryType.EXPENSE, "#84CC16", "fas fa-umbrella", expenses);
        createCategory(user, "Impostos", Category.CategoryType.EXPENSE, "#64748B", "fas fa-file-invoice-dollar", expenses);
        createCategory(user, "Outros", Category.CategoryType.EXPENSE, "#6B7280", "fas fa-question-circle", expenses);
    }
}