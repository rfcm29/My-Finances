package com.example.myfinances.validation;

import com.example.myfinances.exception.SecurityException;
import com.example.myfinances.exception.ValidationException;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class InvestmentValidatorTest {

    private InvestmentValidator validator;
    private User testUser;
    private InvestmentProduct testProduct;

    @BeforeEach
    void setUp() {
        validator = new InvestmentValidator();
        
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        testProduct = InvestmentProduct.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple Inc.")
                .type(InvestmentProduct.InvestmentType.STOCK)
                .currency("USD")
                .build();
    }

    @Test
    void validateInvestmentCreation_WithValidData_DoesNotThrow() {
        // Given
        BigDecimal quantity = new BigDecimal("10.5");
        BigDecimal purchasePrice = new BigDecimal("150.25");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);
        BigDecimal exchangeRate = BigDecimal.ONE;

        // When & Then
        assertThatCode(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, exchangeRate))
                .doesNotThrowAnyException();
    }

    @Test
    void validateInvestmentCreation_WithNullUser_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal purchasePrice = new BigDecimal("150");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                null, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("user");
                });
    }

    @Test
    void validateInvestmentCreation_WithNullProduct_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal purchasePrice = new BigDecimal("150");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, null, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("product");
                });
    }

    @Test
    void validateInvestmentCreation_WithZeroQuantity_ThrowsValidationException() {
        // Given
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal purchasePrice = new BigDecimal("150");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("quantity");
                });
    }

    @Test
    void validateInvestmentCreation_WithNegativeQuantity_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("-10");
        BigDecimal purchasePrice = new BigDecimal("150");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("quantity");
                });
    }

    @Test
    void validateInvestmentCreation_WithTooManyDecimalPlaces_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("10.1234567"); // 7 decimal places
        BigDecimal purchasePrice = new BigDecimal("150");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("quantity");
                });
    }

    @Test
    void validateInvestmentCreation_WithZeroPurchasePrice_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal purchasePrice = BigDecimal.ZERO;
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("purchasePrice");
                });
    }

    @Test
    void validateInvestmentCreation_WithFuturePurchaseDate_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal purchasePrice = new BigDecimal("150");
        LocalDate purchaseDate = LocalDate.now().plusDays(1); // Future date

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("purchaseDate");
                });
    }

    @Test
    void validateInvestmentCreation_WithVeryOldPurchaseDate_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal purchasePrice = new BigDecimal("150");
        LocalDate purchaseDate = LocalDate.of(1800, 1, 1); // Too old

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("purchaseDate");
                });
    }

    @Test
    void validateInvestmentCreation_WithExtremelyHighValue_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("1000");
        BigDecimal purchasePrice = new BigDecimal("2000"); // Total value = 2,000,000
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("totalValue");
                });
    }

    @Test
    void validateInvestmentCreation_WithVerySmallStockQuantity_ThrowsValidationException() {
        // Given
        BigDecimal quantity = new BigDecimal("0.0001"); // Very small quantity
        BigDecimal purchasePrice = new BigDecimal("150");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> validator.validateInvestmentCreation(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationEx = (ValidationException) ex;
                    assertThat(validationEx.getFieldErrors()).containsKey("quantity");
                });
    }

    @Test
    void validateUserOwnership_WithSameUser_DoesNotThrow() {
        // When & Then
        assertThatCode(() -> validator.validateUserOwnership(testUser, testUser))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUserOwnership_WithDifferentUsers_ThrowsSecurityException() {
        // Given
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .name("Other User")
                .build();

        // When & Then
        assertThatThrownBy(() -> validator.validateUserOwnership(testUser, otherUser))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("You can only access your own resources");
    }

    @Test
    void validateUserOwnership_WithNullUsers_ThrowsValidationException() {
        // When & Then
        assertThatThrownBy(() -> validator.validateUserOwnership(null, testUser))
                .isInstanceOf(ValidationException.class);

        assertThatThrownBy(() -> validator.validateUserOwnership(testUser, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateProductSelection_WithValidProduct_DoesNotThrow() {
        // When & Then
        assertThatCode(() -> validator.validateProductSelection(testProduct, testUser))
                .doesNotThrowAnyException();
    }

    @Test
    void validateProductSelection_WithNullProduct_ThrowsValidationException() {
        // When & Then
        assertThatThrownBy(() -> validator.validateProductSelection(null, testUser))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Product selection is required");
    }
}