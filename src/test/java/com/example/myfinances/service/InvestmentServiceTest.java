package com.example.myfinances.service;

import com.example.myfinances.model.Investment;
import com.example.myfinances.model.InvestmentProduct;
import com.example.myfinances.model.User;
import com.example.myfinances.repository.InvestmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private InvestmentProductService investmentProductService;

    @InjectMocks
    private InvestmentService investmentService;

    private User testUser;
    private InvestmentProduct testProduct;
    private Investment testInvestment;

    @BeforeEach
    void setUp() {
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
                .currentPrice(new BigDecimal("150.00"))
                .build();

        testInvestment = Investment.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .quantity(new BigDecimal("10"))
                .purchasePrice(new BigDecimal("100.00"))
                .purchaseDate(LocalDate.now().minusDays(30))
                .exchangeRate(BigDecimal.ONE)
                .build();
    }

    @Test
    void createInvestment_WithValidData_ReturnsInvestment() {
        // Given
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal purchasePrice = new BigDecimal("100.00");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);
        BigDecimal exchangeRate = BigDecimal.ONE;
        String notes = "Test investment";

        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);

        // When
        Investment result = investmentService.createInvestment(
                testUser, testProduct, quantity, purchasePrice, purchaseDate, exchangeRate, notes);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testInvestment);
        verify(investmentRepository).save(any(Investment.class));
    }

    @Test
    void createInvestment_WithoutExchangeRateAndNotes_ReturnsInvestment() {
        // Given
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal purchasePrice = new BigDecimal("100.00");
        LocalDate purchaseDate = LocalDate.now().minusDays(1);

        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);

        // When
        Investment result = investmentService.createInvestment(
                testUser, testProduct, quantity, purchasePrice, purchaseDate);

        // Then
        assertThat(result).isNotNull();
        verify(investmentRepository).save(any(Investment.class));
    }

    @Test
    void updateInvestment_WithValidInvestment_ReturnsUpdatedInvestment() {
        // Given
        when(investmentRepository.save(testInvestment)).thenReturn(testInvestment);

        // When
        Investment result = investmentService.updateInvestment(testInvestment);

        // Then
        assertThat(result).isEqualTo(testInvestment);
        verify(investmentRepository).save(testInvestment);
    }

    @Test
    void deleteInvestment_WithValidInvestment_CallsRepositoryDelete() {
        // When
        investmentService.deleteInvestment(testInvestment);

        // Then
        verify(investmentRepository).delete(testInvestment);
    }

    @Test
    void findByIdAndUser_WithValidIdAndUser_ReturnsInvestment() {
        // Given
        Long investmentId = 1L;
        when(investmentRepository.findByIdAndUser(investmentId, testUser))
                .thenReturn(Optional.of(testInvestment));

        // When
        Optional<Investment> result = investmentService.findByIdAndUser(investmentId, testUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testInvestment);
    }

    @Test
    void findByIdAndUser_WithInvalidId_ReturnsEmpty() {
        // Given
        Long investmentId = 999L;
        when(investmentRepository.findByIdAndUser(investmentId, testUser))
                .thenReturn(Optional.empty());

        // When
        Optional<Investment> result = investmentService.findByIdAndUser(investmentId, testUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_WithValidUser_ReturnsInvestmentList() {
        // Given
        List<Investment> investments = List.of(testInvestment);
        when(investmentRepository.findByUserOrderByPurchaseDateDesc(testUser))
                .thenReturn(investments);

        // When
        List<Investment> result = investmentService.findByUser(testUser);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInvestment);
    }

    @Test
    void findByUserAndType_WithValidUserAndType_ReturnsInvestmentList() {
        // Given
        InvestmentProduct.InvestmentType type = InvestmentProduct.InvestmentType.STOCK;
        List<Investment> investments = List.of(testInvestment);
        when(investmentRepository.findByUserAndProductTypeOrderByPurchaseDateDesc(testUser, type))
                .thenReturn(investments);

        // When
        List<Investment> result = investmentService.findByUserAndType(testUser, type);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInvestment);
    }

    @Test
    void searchInvestments_WithValidSearchTerm_ReturnsInvestmentList() {
        // Given
        String searchTerm = "AAPL";
        List<Investment> investments = List.of(testInvestment);
        when(investmentRepository.searchInvestmentsByUser(testUser, searchTerm))
                .thenReturn(investments);

        // When
        List<Investment> result = investmentService.searchInvestments(testUser, searchTerm);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInvestment);
    }

    @Test
    void searchInvestments_WithEmptySearchTerm_ReturnsAllInvestments() {
        // Given
        String searchTerm = "";
        List<Investment> investments = List.of(testInvestment);
        when(investmentRepository.findByUserOrderByPurchaseDateDesc(testUser))
                .thenReturn(investments);

        // When
        List<Investment> result = investmentService.searchInvestments(testUser, searchTerm);

        // Then
        assertThat(result).hasSize(1);
        verify(investmentRepository).findByUserOrderByPurchaseDateDesc(testUser);
        verify(investmentRepository, never()).searchInvestmentsByUser(any(), any());
    }

    @Test
    void getInvestmentCount_WithValidUser_ReturnsCount() {
        // Given
        long expectedCount = 5L;
        when(investmentRepository.countByUser(testUser)).thenReturn(expectedCount);

        // When
        long result = investmentService.getInvestmentCount(testUser);

        // Then
        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    void getPortfolioSummary_WithValidUser_ReturnsPortfolioSummary() {
        // Given
        List<Investment> investments = List.of(testInvestment);
        when(investmentRepository.findByUserOrderByPurchaseDateDesc(testUser))
                .thenReturn(investments);

        // When
        InvestmentService.PortfolioSummary result = investmentService.getPortfolioSummary(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalInvestments()).isEqualTo(1);
        assertThat(result.getTotalInvested()).isEqualByComparingTo(new BigDecimal("1000.00")); // 10 * 100
        assertThat(result.getCurrentValue()).isEqualByComparingTo(new BigDecimal("1500.00")); // 10 * 150
    }

    @Test
    void hasInvestmentInProduct_WithExistingInvestment_ReturnsTrue() {
        // Given
        when(investmentRepository.existsByUserAndProduct(testUser, testProduct))
                .thenReturn(true);

        // When
        boolean result = investmentService.hasInvestmentInProduct(testUser, testProduct);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasInvestmentInProduct_WithoutExistingInvestment_ReturnsFalse() {
        // Given
        when(investmentRepository.existsByUserAndProduct(testUser, testProduct))
                .thenReturn(false);

        // When
        boolean result = investmentService.hasInvestmentInProduct(testUser, testProduct);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getProfitableInvestments_WithProfitableInvestments_ReturnsFilteredList() {
        // Given
        List<Investment> investments = List.of(testInvestment); // This investment is profitable (150 current vs 100 purchase)
        when(investmentRepository.findByUserOrderByPurchaseDateDesc(testUser))
                .thenReturn(investments);

        // When
        List<Investment> result = investmentService.getProfitableInvestments(testUser);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isProfitable()).isTrue();
    }

    @Test
    void getLosingInvestments_WithLosingInvestments_ReturnsFilteredList() {
        // Given
        // Create a losing investment
        testProduct.setCurrentPrice(new BigDecimal("50.00")); // Lower than purchase price of 100
        List<Investment> investments = List.of(testInvestment);
        when(investmentRepository.findByUserOrderByPurchaseDateDesc(testUser))
                .thenReturn(investments);

        // When
        List<Investment> result = investmentService.getLosingInvestments(testUser);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isProfitable()).isFalse();
    }
}