package com.example.myfinances.service;

import com.example.myfinances.model.AccountCategory;
import com.example.myfinances.model.AccountSubcategory;
import com.example.myfinances.repository.AccountCategoryRepository;
import com.example.myfinances.repository.AccountSubcategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountCategoryService {
    
    private final AccountCategoryRepository accountCategoryRepository;
    private final AccountSubcategoryRepository accountSubcategoryRepository;
    
    /**
     * Get all active categories ordered by display order
     * @return list of active categories
     */
    public List<AccountCategory> getAllActiveCategories() {
        try {
            return accountCategoryRepository.findAllActiveOrderedByDisplayOrder();
        } catch (Exception e) {
            log.error("Error retrieving active categories", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all categories ordered by display order
     * @return list of all categories
     */
    public List<AccountCategory> getAllCategories() {
        return accountCategoryRepository.findAllOrderedByDisplayOrder();
    }
    
    /**
     * Find category by code
     * @param code the category code
     * @return optional category
     */
    public Optional<AccountCategory> findCategoryByCode(String code) {
        return accountCategoryRepository.findByCode(code);
    }
    
    /**
     * Find category by name
     * @param name the category name
     * @return optional category
     */
    public Optional<AccountCategory> findCategoryByName(String name) {
        return accountCategoryRepository.findByName(name);
    }
    
    /**
     * Get all active subcategories for a category
     * @param categoryCode the parent category code
     * @return list of active subcategories
     */
    public List<AccountSubcategory> getActiveSubcategoriesByCategory(String categoryCode) {
        return accountSubcategoryRepository.findActiveByCategoryCodeOrderedByDisplayOrder(categoryCode);
    }
    
    /**
     * Get all subcategories for a category
     * @param categoryCode the parent category code
     * @return list of subcategories
     */
    public List<AccountSubcategory> getSubcategoriesByCategory(String categoryCode) {
        return accountSubcategoryRepository.findByCategoryCodeOrderedByDisplayOrder(categoryCode);
    }
    
    /**
     * Find subcategory by code
     * @param code the subcategory code
     * @return optional subcategory
     */
    public Optional<AccountSubcategory> findSubcategoryByCode(String code) {
        return accountSubcategoryRepository.findByCode(code);
    }
    
    /**
     * Find subcategory by name
     * @param name the subcategory name
     * @return optional subcategory
     */
    public Optional<AccountSubcategory> findSubcategoryByName(String name) {
        return accountSubcategoryRepository.findByName(name);
    }
    
    /**
     * Get categories with their active subcategories as a map
     * @return map of category to list of subcategories
     */
    public Map<AccountCategory, List<AccountSubcategory>> getCategoriesWithActiveSubcategories() {
        List<AccountCategory> categories = getAllActiveCategories();
        Map<AccountCategory, List<AccountSubcategory>> result = new LinkedHashMap<>();
        for (AccountCategory category : categories) {
            List<AccountSubcategory> subcategories = getActiveSubcategoriesByCategory(category.getCode());
            result.put(category, subcategories);
        }
        return result;
    }
    
    /**
     * Get category names for backward compatibility
     * @return list of category names
     */
    public List<String> getCategoryNames() {
        return getAllActiveCategories().stream()
                .map(AccountCategory::getName)
                .collect(Collectors.toList());
    }
    
    /**
     * Get categories with subcategories as string map for backward compatibility
     * @return map of category name to list of subcategory names
     */
    public Map<String, List<String>> getCategoriesWithSubcategoriesAsStrings() {
        try {
            List<AccountCategory> categories = getAllActiveCategories();
            Map<String, List<String>> result = new LinkedHashMap<>();
            for (AccountCategory category : categories) {
                try {
                    List<AccountSubcategory> subcategories = getActiveSubcategoriesByCategory(category.getCode());
                    List<String> subcategoryNames = new ArrayList<>();
                    for (AccountSubcategory subcategory : subcategories) {
                        subcategoryNames.add(subcategory.getName());
                    }
                    result.put(category.getName(), subcategoryNames);
                } catch (Exception e) {
                    log.warn("Error processing subcategories for category: {}", category.getName(), e);
                    // Still add the category with an empty list of subcategories
                    result.put(category.getName(), new ArrayList<>());
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error retrieving categories with subcategories", e);
            return new LinkedHashMap<>();
        }
    }
    
    /**
     * Create a new category
     * @param code the category code
     * @param name the category name
     * @param description the category description
     * @return the created category
     */
    @Transactional
    public AccountCategory createCategory(String code, String name, String description) {
        if (accountCategoryRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Category with code " + code + " already exists");
        }
        
        AccountCategory category = AccountCategory.builder()
                .code(code)
                .name(name)
                .description(description)
                .active(true)
                .build();
        
        return accountCategoryRepository.save(category);
    }
    
    /**
     * Create a new subcategory
     * @param code the subcategory code
     * @param name the subcategory name
     * @param description the subcategory description
     * @param categoryCode the parent category code
     * @return the created subcategory
     */
    @Transactional
    public AccountSubcategory createSubcategory(String code, String name, String description, String categoryCode) {
        if (accountSubcategoryRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Subcategory with code " + code + " already exists");
        }
        
        AccountCategory category = findCategoryByCode(categoryCode)
                .orElseThrow(() -> new IllegalArgumentException("Category with code " + categoryCode + " not found"));
        
        AccountSubcategory subcategory = AccountSubcategory.builder()
                .code(code)
                .name(name)
                .description(description)
                .category(category)
                .active(true)
                .build();
        
        return accountSubcategoryRepository.save(subcategory);
    }
}