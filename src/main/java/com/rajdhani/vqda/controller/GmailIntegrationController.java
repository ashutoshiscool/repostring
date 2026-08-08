package com.rajdhani.vqda.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rajdhani.vqda.model.GmailIntegration;
import com.rajdhani.vqda.repository.GmailIntegrationRepository;
import com.rajdhani.vqda.service.EmailService;
import com.rajdhani.vqda.service.TokenEncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/integrations/gmail")
public class GmailIntegrationController {

    @Autowired
    private GmailIntegrationRepository gmailIntegrationRepository;

    @Autowired
    private TokenEncryptionService tokenEncryptionService;

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
                .filter(g -> g.getEncryptedClientSecret() != null && !g.getEncryptedClientSecret().trim().isEmpty())
                .findFirst().orElse(null);
        if (gi != null) {
            return tokenEncryptionService.decrypt(gi.getEncryptedClientSecret().trim());
        }
        return envClientSecret != null ? envClientSecret.trim() : "";
    }

    @PostMapping("/save-credentials")
    public String saveCredentials(@RequestParam("clientId") String inputClientId,
                                  @RequestParam("clientSecret") String inputClientSecret) {

        GmailIntegration integration = gmailIntegrationRepository.findAll().stream().findFirst()
                .orElse(new GmailIntegration());

        if (integration.getEmail() == null || integration.getEmail().isEmpty()) {
            integration.setEmail("pending.setup@hospital.com");
        }

        integration.setClientId(inputClientId.trim());
        integration.setEncryptedClientSecret(tokenEncryptionService.encrypt(inputClientSecret.trim()));
        gmailIntegrationRepository.save(integration);

        return "redirect:/admin/smtp?success=CredentialsSaved";
    }

    @GetMapping("/connect")
    public String connectGmail(HttpSession session) {
        String activeClientId = getActiveClientId();
        String activeClientSecret = getActiveClientSecret();

        if (activeClientId.isEmpty() || activeClientSecret.isEmpty()) {
            return "redirect:/admin/smtp?error=MissingClientCredentials";
        }

        // Generate cryptographically secure OAuth state
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        session.setAttribute("GMAIL_OAUTH_STATE", state);

        try {
            String scope = "https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/userinfo.email";
            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?"
                    + "client_id=" + URLEncoder.encode(activeClientId, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
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
            HttpSession session) {

        if (error != null && !error.isEmpty()) {
            return "redirect:/admin/smtp?error=AccessDenied";
        }

        String sessionState = (String) session.getAttribute("GMAIL_OAUTH_STATE");
        if (sessionState == null || state == null || !sessionState.equals(state)) {
            return "redirect:/admin/smtp?error=InvalidState";
        }
        session.removeAttribute("GMAIL_OAUTH_STATE");

        if (code == null || code.trim().isEmpty()) {
            return "redirect:/admin/smtp?error=NoCode";
        }

        String activeClientId = getActiveClientId();
        String activeClientSecret = getActiveClientSecret();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = "client_id=" + URLEncoder.encode(activeClientId, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(activeClientSecret, StandardCharsets.UTF_8)
                    + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code"
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "redirect:/admin/smtp?error=TokenExchangeFailed";
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String accessToken = json.get("access_token").getAsString();
            int expiresIn = json.get("expires_in").getAsInt();
            String refreshToken = json.has("refresh_token") ? json.get("refresh_token").getAsString() : null;

            // Fetch user email from Google UserInfo API
            HttpRequest userRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> userResponse = client.send(userRequest, HttpResponse.BodyHandlers.ofString());
            String connectedEmail = "admin@hospital.com";
            String subId = null;

            if (userResponse.statusCode() == 200) {
                JsonObject userJson = JsonParser.parseString(userResponse.body()).getAsJsonObject();
                if (userJson.has("email")) {
                    connectedEmail = userJson.get("email").getAsString();
                }
                if (userJson.has("id")) {
                    subId = userJson.get("id").getAsString();
                }
            }

            GmailIntegration integration = gmailIntegrationRepository.findByEmail(connectedEmail)
                    .orElse(gmailIntegrationRepository.findAll().stream().findFirst().orElse(new GmailIntegration()));

            integration.setEmail(connectedEmail);
            integration.setGoogleSubjectId(subId);
            if (refreshToken != null) {
                integration.setEncryptedRefreshToken(tokenEncryptionService.encrypt(refreshToken));
            }
            integration.setAccessToken(accessToken);
            integration.setAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            integration.setScopes("https://www.googleapis.com/auth/gmail.send");
            integration.setConnected(true);
            integration.setStatus("CONNECTED");
            integration.setLastConnectedAt(LocalDateTime.now());

            gmailIntegrationRepository.save(integration);

            return "redirect:/admin/smtp?success=Connected";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/smtp?error=CallbackError";
        }
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> map = new HashMap<>();
        GmailIntegration integration = gmailIntegrationRepository.findAll().stream()
                .filter(GmailIntegration::isConnected)
                .findFirst().orElse(null);

        if (integration != null) {
            map.put("connected", true);
            map.put("email", integration.getEmail());
            map.put("status", integration.getStatus());
            map.put("lastConnectedAt", integration.getLastConnectedAt());
        } else {
            map.put("connected", false);
            map.put("email", null);
            map.put("status", "DISCONNECTED");
        }

        return ResponseEntity.ok(map);
    }

    @PostMapping("/disconnect")
    public String disconnectGmail() {
        gmailIntegrationRepository.findAll().forEach(gi -> {
            gi.setConnected(false);
            gi.setStatus("DISCONNECTED");
            gmailIntegrationRepository.save(gi);
        });
        return "redirect:/admin/smtp?success=Disconnected";
    }

    @PostMapping("/test")
    public String sendTestEmail(@RequestParam("toEmail") String toEmail) {
        try {
            emailService.sendTestEmail(toEmail);
            return "redirect:/admin/smtp?success=TestEmailSent";
        } catch (Exception e) {
            return "redirect:/admin/smtp?error=TestEmailFailed";
        }
    }
}
