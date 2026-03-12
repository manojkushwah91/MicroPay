package com.micropay.auth.repository;

import com.micropay.auth.model.BlockedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BlockedTokenRepository extends JpaRepository<BlockedToken, UUID> {
    boolean existsByToken(String token);
}
