package com.rajdhani.vqda.repository;

import com.rajdhani.vqda.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findFirstByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(String email, String type);
    Optional<OtpToken> findFirstByEmailAndOtpCodeAndTypeAndVerifiedFalse(String email, String otpCode, String type);
}
