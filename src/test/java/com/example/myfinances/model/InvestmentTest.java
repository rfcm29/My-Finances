package com.example.myfinances.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class InvestmentTest {

    private Investment investment;
    private InvestmentProduct product;

    @BeforeEach
    void setUp() {
        product = InvestmentProduct.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple Inc.")
                .type(InvestmentProduct.InvestmentType.STOCK)
                .currency("USD")
                .currentPrice(new BigDecimal("150.00"))
                .build();

        investment = Investment.builder()
                .id(1L)
                .product(product)
                .quantity(new BigDecimal("10"))
                .purchasePrice(new BigDecimal("100.00"))
                .purchaseDate(LocalDate.now().minusDays(30))
                .exchangeRate(BigDecimal.ONE)
                .build();
    }

    @Test
    void getTotalInvested_WithValidData_ReturnsCorrectAmount() {
        // When
        BigDecimal result = investment.getTotalInvested();

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("1000.00")); // 10 * 100
    }

    @Test
    void getTotalInvested_WithNullValues_ReturnsZero() {
        // Given
        investment.setQuantity(null);

        // When
        BigDecimal result = investment.getTotalInvested();

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCurrentValue_WithValidData_ReturnsCorrectAmount() {
        // When
        BigDecimal result = investment.getCurrentValue();

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("1500.00")); // 10 * 150
    }

    @Test
    void getCurrentValue_WithNullCurrentPrice_ReturnsZero() {
        // Given
        product.setCurrentPrice(null);

        // When
        BigDecimal result = investment.getCurrentValue();

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getTotalGainLoss_WithProfitableInvestment_ReturnsPositiveAmount() {
        // When
        BigDecimal result = investment.getTotalGainLoss();

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("500.00")); // 1500 - 1000
    }

    @Test
    void getTotalGainLoss_WithLosingInvestment_ReturnsNegativeAmount() {
        // Given
        product.setCurrentPrice(new BigDecimal("50.00")); // Lower than purchase price

        // When
        BigDecimal result = investment.getTotalGainLoss();

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("-500.00")); // 500 - 1000
    }

    @Test
    void getPercentageGainLoss_WithProfitableInvestment_ReturnsPositivePercentage() {
        // When
        BigDecimal result = investment.getPercentageGainLoss();

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("50.00")); // 50% gain
    }

    @Test
    void getPercentageGainLoss_WithZeroInvestment_ReturnsZero() {
        // Given
        investment.setQuantity(BigDecimal.ZERO);

        // When
        BigDecimal result = investment.getPercentageGainLoss();

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void isProfitable_WithProfitableInvestment_ReturnsTrue() {
        // When
        boolean result = investment.isProfitable();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isProfitable_WithLosingInvestment_ReturnsFalse() {
        // Given
        product.setCurrentPrice(new BigDecimal("50.00"));

        // When
        boolean result = investment.isProfitable();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getGainLossCssClass_WithProfitableInvestment_ReturnsSuccessClass() {
        // When
        String result = investment.getGainLossCssClass();

        // Then
        assertThat(result).isEqualTo("text-success");
    }

    @Test
    void getGainLossCssClass_WithLosingInvestment_ReturnsDangerClass() {
        // Given
        product.setCurrentPrice(new BigDecimal("50.00"));

        // When
        String result = investment.getGainLossCssClass();

        // Then
        assertThat(result).isEqualTo("text-danger");
    }

    @Test
    void getGainLossCssClass_WithBreakEvenInvestment_ReturnsMutedClass() {
        // Given
        product.setCurrentPrice(new BigDecimal("100.00")); // Same as purchase price

        // When
        String result = investment.getGainLossCssClass();

        // Then
        assertThat(result).isEqualTo("text-muted");
    }

    @Test
    void getFormattedQuantity_WithCryptocurrency_ReturnsFullPrecision() {
        // Given
        product.setType(InvestmentProduct.InvestmentType.CRYPTOCURRENCY);
        investment.setQuantity(new BigDecimal("0.12345678"));

        // When
        String result = investment.getFormattedQuantity();

        // Then
        assertThat(result).isEqualTo("0.12345678");
    }

    @Test
    void getFormattedQuantity_WithStock_ReturnsLimitedPrecision() {
        // Given
        investment.setQuantity(new BigDecimal("10.123456"));

        // When
        String result = investment.getFormattedQuantity();

        // Then
        assertThat(result).isEqualTo("10.123");
    }

    @Test
    void getFormattedTotalInvested_ReturnsFormattedString() {
        // When
        String result = investment.getFormattedTotalInvested();

        // Then
        assertThat(result).isEqualTo("1000.00 USD");
    }

    @Test
    void getFormattedCurrentValue_ReturnsFormattedString() {
        // When
        String result = investment.getFormattedCurrentValue();

        // Then
        assertThat(result).isEqualTo("1500.00 USD");
    }

    @Test
    void getFormattedTotalGainLoss_WithPositiveGain_ReturnsFormattedStringWithPlus() {
        // When
        String result = investment.getFormattedTotalGainLoss();

        // Then
        assertThat(result).isEqualTo("+500.00 USD");
    }

    @Test
    void getFormattedTotalGainLoss_WithLoss_ReturnsFormattedStringWithMinus() {
        // Given
        product.setCurrentPrice(new BigDecimal("50.00"));

        // When
        String result = investment.getFormattedTotalGainLoss();

        // Then
        assertThat(result).isEqualTo("-500.00 USD");
    }

    @Test
    void getFormattedPercentageGainLoss_WithPositiveGain_ReturnsFormattedStringWithPlus() {
        // When
        String result = investment.getFormattedPercentageGainLoss();

        // Then
        assertThat(result).isEqualTo("+50.00%");
    }

    @Test
    void getTotalInvestedInBaseCurrency_WithExchangeRate_ReturnsConvertedAmount() {
        // Given
        investment.setExchangeRate(new BigDecimal("1.2")); // USD to EUR rate

        // When
        BigDecimal result = investment.getTotalInvestedInBaseCurrency();

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("833.33")); // 1000 / 1.2
    }

    @Test
    void getCurrentValueInBaseCurrency_WithExchangeRate_ReturnsConvertedAmount() {
        // Given
        investment.setExchangeRate(new BigDecimal("1.2"));

        // When
        BigDecimal result = investment.getCurrentValueInBaseCurrency();

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("1250.00")); // 1500 / 1.2
    }
}