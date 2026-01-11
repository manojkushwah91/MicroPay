package com.micropay.wallet.repository;

import com.micropay.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Wallet entity
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /**
     * Find wallet by user ID with pessimistic lock for concurrent updates
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    Optional<Wallet> findByUserIdWithLock(UUID userId);

    /**
     * Find wallet by user ID
     */
    Optional<Wallet> findByUserId(UUID userId);

    /**
     * Check if wallet exists for user
     */
    boolean existsByUserId(UUID userId);
}




