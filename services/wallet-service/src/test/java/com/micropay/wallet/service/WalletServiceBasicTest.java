package com.micropay.wallet.service;

import com.micropay.wallet.dto.WalletResponse;
import com.micropay.wallet.exception.InsufficientBalanceException;
import com.micropay.wallet.exception.WalletNotFoundException;
import com.micropay.wallet.model.Wallet;
import com.micropay.wallet.model.WalletStatus;
import com.micropay.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet Service Basic Tests")
class WalletServiceBasicTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private WalletService walletService;

    private UUID userId;
    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("100.00"));
        wallet.setCurrency("USD");
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get wallet by user ID successfully")
    void getWalletByUserId_Success() {
        // Given
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        // When
        WalletResponse response = walletService.getWalletByUserId(userId);

        // Then
        assertNotNull(response);
        assertEquals(walletId, response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(new BigDecimal("100.00"), response.getBalance());
        assertEquals("USD", response.getCurrency());
        assertEquals(WalletStatus.ACTIVE.name(), response.getStatus());

        verify(walletRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should auto-create wallet when not found")
    void getWalletByUserId_AutoCreate() {
        // Given
        Wallet newWallet = new Wallet();
        newWallet.setId(walletId);
        newWallet.setUserId(userId);
        newWallet.setBalance(BigDecimal.ZERO);
        newWallet.setCurrency("USD");
        newWallet.setStatus(WalletStatus.ACTIVE);
        newWallet.setCreatedAt(LocalDateTime.now());
        newWallet.setUpdatedAt(LocalDateTime.now());
        
        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(newWallet));

        // When
        WalletResponse response = walletService.getWalletByUserId(userId);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getUserId());

        verify(walletRepository, times(2)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should create wallet successfully")
    void createWallet_Success() {
        // Given
        Wallet newWallet = new Wallet();
        newWallet.setId(walletId);
        newWallet.setUserId(userId);
        newWallet.setBalance(BigDecimal.ZERO);
        newWallet.setCurrency("USD");
        newWallet.setStatus(WalletStatus.ACTIVE);
        newWallet.setCreatedAt(LocalDateTime.now());
        newWallet.setUpdatedAt(LocalDateTime.now());
        
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        // When
        Wallet result = walletService.createWallet(userId, "USD");

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("USD", result.getCurrency());
        assertEquals(BigDecimal.ZERO, result.getBalance());

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should handle concurrent wallet creation")
    void createWallet_ConcurrentCreation() {
        // Given
        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // When
        Wallet result = walletService.createWallet(userId, "USD");

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());

        verify(walletRepository, times(2)).findByUserId(userId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should top up wallet successfully")
    void topUpWallet_Success() {
        // Given
        BigDecimal topUpAmount = new BigDecimal("50.00");
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletResponse response = walletService.topUpWallet(userId, topUpAmount);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByUserIdWithLock(userId);
        verify(walletRepository).save(wallet);
        verify(kafkaTemplate).send(eq("wallet.balance.updated"), eq(userId.toString()), any());
    }

    @Test
    @DisplayName("Should credit wallet successfully")
    void creditWallet_Success() {
        // Given
        BigDecimal creditAmount = new BigDecimal("25.00");
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletResponse response = walletService.creditWallet(userId, creditAmount, "TXN-001");

        // Then
        assertNotNull(response);
        verify(walletRepository).findByUserIdWithLock(userId);
        verify(walletRepository).save(wallet);
        verify(kafkaTemplate).send(eq("wallet.balance.updated"), eq(userId.toString()), any());
    }

    @Test
    @DisplayName("Should auto-create wallet when crediting non-existent wallet")
    void creditWallet_AutoCreate() {
        // Given
        BigDecimal creditAmount = new BigDecimal("25.00");
        when(walletRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletResponse response = walletService.creditWallet(userId, creditAmount, "TXN-001");

        // Then
        assertNotNull(response);
        verify(walletRepository, times(2)).findByUserIdWithLock(userId);
        verify(walletRepository, atLeastOnce()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should throw exception when crediting inactive wallet")
    void creditWallet_InactiveWallet() {
        // Given
        wallet.setStatus(WalletStatus.SUSPENDED);
        BigDecimal creditAmount = new BigDecimal("25.00");
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            walletService.creditWallet(userId, creditAmount, "TXN-001"));

        verify(walletRepository).findByUserIdWithLock(userId);
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should debit wallet successfully")
    void debitWallet_Success() {
        // Given
        BigDecimal debitAmount = new BigDecimal("30.00");
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletResponse response = walletService.debitWallet(userId, debitAmount, "TXN-002");

        // Then
        assertNotNull(response);
        verify(walletRepository).findByUserIdWithLock(userId);
        verify(walletRepository).save(wallet);
        verify(kafkaTemplate).send(eq("wallet.balance.updated"), eq(userId.toString()), any());
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when insufficient funds")
    void debitWallet_InsufficientBalance() {
        // Given
        BigDecimal debitAmount = new BigDecimal("150.00");
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));

        // When & Then
        assertThrows(InsufficientBalanceException.class, () -> 
            walletService.debitWallet(userId, debitAmount, "TXN-002"));

        verify(walletRepository).findByUserIdWithLock(userId);
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when debiting inactive wallet")
    void debitWallet_InactiveWallet() {
        // Given
        wallet.setStatus(WalletStatus.SUSPENDED);
        BigDecimal debitAmount = new BigDecimal("30.00");
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            walletService.debitWallet(userId, debitAmount, "TXN-002"));

        verify(walletRepository).findByUserIdWithLock(userId);
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should auto-create wallet when debiting non-existent wallet")
    void debitWallet_AutoCreate() {
        // Given
        BigDecimal debitAmount = new BigDecimal("30.00");
        when(walletRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletResponse response = walletService.debitWallet(userId, debitAmount, "TXN-002");

        // Then
        assertNotNull(response);
        verify(walletRepository, times(2)).findByUserIdWithLock(userId);
        verify(walletRepository, atLeastOnce()).save(any(Wallet.class));
    }
}
