package com.rajdhani.vqda.repository;

import com.rajdhani.vqda.model.GmailIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GmailIntegrationRepository extends JpaRepository<GmailIntegration, Long> {
    Optional<GmailIntegration> findFirstByConnectedTrue();
    Optional<GmailIntegration> findByEmail(String email);
}
