package com.micropay.auth.repository;

import com.micropay.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    PasswordResetToken findByToken(String token);
}
