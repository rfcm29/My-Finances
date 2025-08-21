package com.example.myfinances.repository;

import com.example.myfinances.model.AccountCategory;
import com.example.myfinances.model.AccountSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountSubcategoryRepository extends JpaRepository<AccountSubcategory, Long> {
    
    /**
     * Find subcategory by code
     * @param code the unique subcategory code
     * @return optional subcategory
     */
    Optional<AccountSubcategory> findByCode(String code);
    
    /**
     * Find subcategory by name
     * @param name the subcategory name
     * @return optional subcategory
     */
    Optional<AccountSubcategory> findByName(String name);
    
    /**
     * Find subcategories by category
     * @param category the parent category
     * @return list of subcategories
     */
    List<AccountSubcategory> findByCategoryOrderByDisplayOrderAscNameAsc(AccountCategory category);
    
    /**
     * Find active subcategories by category
     * @param category the parent category
     * @return list of active subcategories
     */
    @Query("SELECT s FROM AccountSubcategory s WHERE s.category = :category AND s.active = true ORDER BY s.displayOrder ASC, s.name ASC")
    List<AccountSubcategory> findActiveByCategoryOrderedByDisplayOrder(@Param("category") AccountCategory category);
    
    /**
     * Find subcategories by category code
     * @param categoryCode the parent category code
     * @return list of subcategories
     */
    @Query("SELECT s FROM AccountSubcategory s WHERE s.category.code = :categoryCode ORDER BY s.displayOrder ASC, s.name ASC")
    List<AccountSubcategory> findByCategoryCodeOrderedByDisplayOrder(@Param("categoryCode") String categoryCode);
    
    /**
     * Find active subcategories by category code
     * @param categoryCode the parent category code
     * @return list of active subcategories
     */
    @Query("SELECT s FROM AccountSubcategory s WHERE s.category.code = :categoryCode AND s.active = true ORDER BY s.displayOrder ASC, s.name ASC")
    List<AccountSubcategory> findActiveByCategoryCodeOrderedByDisplayOrder(@Param("categoryCode") String categoryCode);
    
    /**
     * Find subcategory by code and category code
     * @param code the subcategory code
     * @param categoryCode the parent category code
     * @return optional subcategory
     */
    @Query("SELECT s FROM AccountSubcategory s WHERE s.code = :code AND s.category.code = :categoryCode")
    Optional<AccountSubcategory> findByCodeAndCategoryCode(@Param("code") String code, @Param("categoryCode") String categoryCode);
    
    /**
     * Check if subcategory exists by code
     * @param code the subcategory code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Check if subcategory exists by name and category
     * @param name the subcategory name
     * @param category the parent category
     * @return true if exists
     */
    boolean existsByNameAndCategory(String name, AccountCategory category);
    
    /**
     * Find all active subcategories
     * @return list of active subcategories
     */
    @Query("SELECT s FROM AccountSubcategory s WHERE s.active = true ORDER BY s.category.displayOrder ASC, s.displayOrder ASC, s.name ASC")
    List<AccountSubcategory> findAllActiveOrderedByCategoryAndDisplayOrder();
    
    /**
     * Find subcategories by active status
     * @param active the active status
     * @return list of subcategories
     */
    List<AccountSubcategory> findByActiveOrderByDisplayOrderAscNameAsc(boolean active);
}