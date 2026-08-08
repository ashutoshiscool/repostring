package com.rajdhani.vqda.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajdhani.vqda.model.GmailIntegration;
import com.rajdhani.vqda.repository.GmailIntegrationRepository;
import com.rajdhani.vqda.service.EmailService;
import com.rajdhani.vqda.service.GmailApiService;
import com.rajdhani.vqda.service.TokenEncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/integrations/gmail")
public class GmailIntegrationController {

    @Autowired
    private GmailIntegrationRepository gmailIntegrationRepository;

    @Autowired
    private TokenEncryptionService tokenEncryptionService;

    @Autowired
    private GmailApiService gmailApiService;

    @Autowired
    private EmailService emailService;

    @Value("${google.oauth.client-id:}")
    private String envClientId;

    @Value("${google.oauth.client-secret:}")
    private String envClientSecret;

    @Value("${google.oauth.redirect-uri:http://localhost:8080/api/integrations/gmail/callback}")
    private String redirectUri;

    public String getActiveClientId() {
        GmailIntegration gi = gmailIntegrationRepository.findAll().stream()
                .filter(g -> g.getClientId() != null && !g.getClientId().trim().isEmpty())
                .findFirst().orElse(null);
        if (gi != null) {
            return gi.getClientId().trim();
        }
        return envClientId != null ? envClientId.trim() : "";
    }

    public String getActiveClientSecret() {
        GmailIntegration gi = gmailIntegrationRepository.findAll().stream()
                .filter(g -> g.getClientSecret() != null && !g.getClientSecret().trim().isEmpty())
                .findFirst().orElse(null);
        if (gi != null) {
            return gi.getClientSecret().trim();
        }
        return envClientSecret != null ? envClientSecret.trim() : "";
    }

    private String buildRedirectUri(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/api/integrations/gmail/callback")
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    @PostMapping("/save-credentials")
    public String saveCredentials(
            @RequestParam("clientId") String clientId,
            @RequestParam("clientSecret") String clientSecret) {
        
        GmailIntegration integration = gmailIntegrationRepository.findAll().stream()
                .findFirst().orElse(new GmailIntegration());

        integration.setClientId(clientId.trim());
        integration.setClientSecret(clientSecret.trim());
        gmailIntegrationRepository.save(integration);

        return "redirect:/admin/smtp?success=CredentialsSaved";
    }

    @GetMapping("/connect")
    public String connectGmail(HttpServletRequest request, HttpSession session) {
        String activeClientId = getActiveClientId();
        String activeClientSecret = getActiveClientSecret();

        if (activeClientId.isEmpty() || activeClientSecret.isEmpty()) {
            return "redirect:/admin/smtp?error=MissingClientCredentials";
        }

        String computedRedirectUri = buildRedirectUri(request);

        // Generate cryptographically secure OAuth state
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        session.setAttribute("GMAIL_OAUTH_STATE", state);
        session.setAttribute("GMAIL_OAUTH_REDIRECT_URI", computedRedirectUri);

        try {
            String scope = "https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/userinfo.email";
            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?"
                    + "client_id=" + URLEncoder.encode(activeClientId, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(computedRedirectUri, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8)
                    + "&access_type=offline"
                    + "&prompt=consent"
                    + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

            return "redirect:" + authUrl;
        } catch (Exception e) {
            return "redirect:/admin/smtp?error=AuthUrlError";
        }
    }

    @GetMapping("/callback")
    public String oauthCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request,
            HttpSession session) {

        if (error != null && !error.isEmpty()) {
            return "redirect:/admin/smtp?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
        }

        String savedState = (String) session.getAttribute("GMAIL_OAUTH_STATE");
        if (savedState == null || !savedState.equals(state)) {
            return "redirect:/admin/smtp?error=InvalidState";
        }

        String activeClientId = getActiveClientId();
        String activeClientSecret = getActiveClientSecret();
        String savedRedirectUri = (String) session.getAttribute("GMAIL_OAUTH_REDIRECT_URI");
        if (savedRedirectUri == null) {
            savedRedirectUri = buildRedirectUri(request);
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&client_id=" + URLEncoder.encode(activeClientId, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(activeClientSecret, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(savedRedirectUri, StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code";

            HttpRequest tokenReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(tokenReq, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (root.has("error")) {
                String errText = root.get("error").asText();
                return "redirect:/admin/smtp?error=" + URLEncoder.encode(errText, StandardCharsets.UTF_8);
            }

            String accessToken = root.get("access_token").asText();
            String refreshToken = root.has("refresh_token") ? root.get("refresh_token").asText() : null;

            // Fetch user email from Google UserInfo API
            HttpRequest userReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> userResp = client.send(userReq, HttpResponse.BodyHandlers.ofString());
            JsonNode userJson = mapper.readTree(userResp.body());

            String userEmail = userJson.has("email") ? userJson.get("email").asText() : "unknown@gmail.com";

            // Find or create integration entity
            GmailIntegration integration = gmailIntegrationRepository.findAll().stream()
                    .findFirst().orElse(new GmailIntegration());

            integration.setEmail(userEmail);
            integration.setConnected(true);
            integration.setStatus("CONNECTED");
            integration.setClientId(activeClientId);
            integration.setClientSecret(activeClientSecret);
            integration.setScopes("https://www.googleapis.com/auth/gmail.send");
            integration.setLastConnectedAt(LocalDateTime.now());

            if (refreshToken != null && !refreshToken.isEmpty()) {
                String encryptedRefreshToken = tokenEncryptionService.encrypt(refreshToken);
                integration.setEncryptedRefreshToken(encryptedRefreshToken);
            }

            gmailIntegrationRepository.save(integration);

            return "redirect:/admin/smtp?success=Connected";

        } catch (Exception e) {
            return "redirect:/admin/smtp?error=TokenExchangeFailed";
        }
    }

    @PostMapping("/disconnect")
    public String disconnectGmail() {
        GmailIntegration integration = gmailIntegrationRepository.findAll().stream()
                .findFirst().orElse(null);
        if (integration != null) {
            integration.setConnected(false);
            integration.setStatus("DISCONNECTED");
            integration.setEncryptedRefreshToken(null);
            gmailIntegrationRepository.save(integration);
        }
        return "redirect:/admin/smtp?success=Disconnected";
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> resp = new HashMap<>();
        GmailIntegration integration = gmailIntegrationRepository.findAll().stream()
                .findFirst().orElse(null);

        if (integration != null && Boolean.TRUE.equals(integration.getConnected())) {
            resp.put("connected", true);
            resp.put("email", integration.getEmail());
            resp.put("status", integration.getStatus());
        } else {
            resp.put("connected", false);
            resp.put("email", null);
            resp.put("status", "DISCONNECTED");
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/test")
    public String sendTestEmail(@RequestParam("toEmail") String toEmail) {
        try {
            emailService.sendSimpleEmail(
                    toEmail,
                    "VirtualQueue - Gmail API OAuth 2.0 Integration Test",
                    "Hello!\n\nThis is an automated test email dispatched via the official Google Gmail API (OAuth 2.0) from VirtualQueue Application.\n\nAll systems operational."
            );
            return "redirect:/admin/smtp?success=TestEmailSent";
        } catch (Exception e) {
            return "redirect:/admin/smtp?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }
}
