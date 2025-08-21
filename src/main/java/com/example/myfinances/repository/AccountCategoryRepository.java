package com.example.myfinances.repository;

import com.example.myfinances.model.AccountCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountCategoryRepository extends JpaRepository<AccountCategory, Long> {
    
    /**
     * Find category by code
     * @param code the unique category code
     * @return optional category
     */
    Optional<AccountCategory> findByCode(String code);
    
    /**
     * Find category by name
     * @param name the category name
     * @return optional category
     */
    Optional<AccountCategory> findByName(String name);
    
    /**
     * Find all active categories ordered by display order
     * @return list of active categories
     */
    @Query("SELECT c FROM AccountCategory c WHERE c.active = true ORDER BY c.displayOrder ASC, c.name ASC")
    List<AccountCategory> findAllActiveOrderedByDisplayOrder();
    
    /**
     * Find all categories ordered by display order
     * @return list of all categories
     */
    @Query("SELECT c FROM AccountCategory c ORDER BY c.displayOrder ASC, c.name ASC")
    List<AccountCategory> findAllOrderedByDisplayOrder();
    
    /**
     * Check if category exists by code
     * @param code the category code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Check if category exists by name
     * @param name the category name
     * @return true if exists
     */
    boolean existsByName(String name);
    
    /**
     * Find categories by active status
     * @param active the active status
     * @return list of categories
     */
    List<AccountCategory> findByActiveOrderByDisplayOrderAscNameAsc(boolean active);
}