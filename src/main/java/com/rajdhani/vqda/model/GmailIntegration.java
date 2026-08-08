package com.rajdhani.vqda.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "gmail_integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GmailIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "client_id", length = 500)
    private String clientId;

    @Column(name = "encrypted_client_secret", length = 500)
    private String encryptedClientSecret;

    @Column(name = "google_subject_id")
    private String googleSubjectId;

    @Column(name = "encrypted_refresh_token", length = 1000)
    private String encryptedRefreshToken;

    @Column(name = "access_token", length = 2000)
    private String accessToken;

    @Column(name = "access_token_expires_at")
    private LocalDateTime accessTokenExpiresAt;

    @Column(length = 500)
    private String scopes;

    @Column(nullable = false)
    private boolean connected = false;

    @Column(nullable = false)
    private String status = "DISCONNECTED"; // CONNECTED, EXPIRED, DISCONNECTED

    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
