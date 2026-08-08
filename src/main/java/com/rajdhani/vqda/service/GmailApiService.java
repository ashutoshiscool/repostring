package com.rajdhani.vqda.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.rajdhani.vqda.model.GmailIntegration;
import com.rajdhani.vqda.repository.GmailIntegrationRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Properties;

@Service
public class GmailApiService {

    @Autowired
    private GmailIntegrationRepository gmailIntegrationRepository;

    @Autowired
    private TokenEncryptionService tokenEncryptionService;

    @Value("${google.oauth.client-id:}")
    private String envClientId;

    @Value("${google.oauth.client-secret:}")
    private String envClientSecret;

    public String getActiveClientId(GmailIntegration integration) {
        if (integration != null && integration.getClientId() != null && !integration.getClientId().trim().isEmpty()) {
            return integration.getClientId().trim();
        }
        return envClientId;
    }

    public String getActiveClientSecret(GmailIntegration integration) {
        if (integration != null && integration.getEncryptedClientSecret() != null && !integration.getEncryptedClientSecret().trim().isEmpty()) {
            return tokenEncryptionService.decrypt(integration.getEncryptedClientSecret().trim());
        }
        return envClientSecret;
    }

    public void sendEmail(String to, String subject, String htmlContent, String plainText, String cc, String bcc) {
        System.out.println("=================================================================");
        System.out.println(" [GMAIL API DISPATCH] To: " + to + " | Subject: " + subject);
        System.out.println("=================================================================");

        GmailIntegration integration = gmailIntegrationRepository.findAll().stream()
                .filter(GmailIntegration::isConnected)
                .findFirst().orElse(null);

        if (integration == null) {
            System.out.println("⚠️ [GMAIL API NOTICE] No active Gmail account connected. Content logged safely above.");
            return;
        }

        try {
            String accessToken = getOrRefreshAccessToken(integration);
            if (accessToken == null) {
                System.out.println("⚠️ [GMAIL API NOTICE] Could not obtain active OAuth access token.");
                return;
            }

            // Build RFC 2822 MIME Message
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.setFrom(new InternetAddress(integration.getEmail()));
            mimeMessage.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));

            if (cc != null && !cc.trim().isEmpty()) {
                mimeMessage.addRecipient(jakarta.mail.Message.RecipientType.CC, new InternetAddress(cc.trim()));
            }

            if (bcc != null && !bcc.trim().isEmpty()) {
                mimeMessage.addRecipient(jakarta.mail.Message.RecipientType.BCC, new InternetAddress(bcc.trim()));
            }

            mimeMessage.setSubject(subject, "UTF-8");

            if (htmlContent != null && !htmlContent.trim().isEmpty()) {
                mimeMessage.setContent(htmlContent, "text/html; charset=utf-8");
            } else {
                mimeMessage.setText(plainText != null ? plainText : "", "UTF-8");
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            byte[] rawBytes = buffer.toByteArray();
            String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);

            Message gmailMessage = new Message();
            gmailMessage.setRaw(encodedEmail);

            // Execute Gmail API users.messages.send
            Gmail service = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
            ).setApplicationName("VirtualQueue").build();

            service.users().messages().send("me", gmailMessage).execute();
            System.out.println("✅ [GMAIL API SUCCESS] Email sent via Gmail API from " + integration.getEmail() + " to " + to);

        } catch (Exception e) {
            System.err.println("⚠️ [GMAIL API DISPATCH NOTICE] Could not send via Gmail API (" + e.getMessage() + "). Message logged safely above.");
        }
    }

    public synchronized String getOrRefreshAccessToken(GmailIntegration integration) {
        if (integration == null || !integration.isConnected()) {
            return null;
        }

        // Return cached token if valid for at least 60 seconds
        if (integration.getAccessToken() != null && integration.getAccessTokenExpiresAt() != null
                && LocalDateTime.now().plusSeconds(60).isBefore(integration.getAccessTokenExpiresAt())) {
            return integration.getAccessToken();
        }

        String refreshToken = tokenEncryptionService.decrypt(integration.getEncryptedRefreshToken());
        if (refreshToken == null || refreshToken.isEmpty()) {
            integration.setConnected(false);
            integration.setStatus("EXPIRED");
            gmailIntegrationRepository.save(integration);
            return null;
        }

        String activeClientId = getActiveClientId(integration);
        String activeClientSecret = getActiveClientSecret(integration);

        try {
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = "client_id=" + java.net.URLEncoder.encode(activeClientId, "UTF-8")
                    + "&client_secret=" + java.net.URLEncoder.encode(activeClientSecret, "UTF-8")
                    + "&refresh_token=" + java.net.URLEncoder.encode(refreshToken, "UTF-8")
                    + "&grant_type=refresh_token";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                String newAccessToken = json.get("access_token").getAsString();
                int expiresIn = json.get("expires_in").getAsInt();

                integration.setAccessToken(newAccessToken);
                integration.setAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                integration.setStatus("CONNECTED");
                gmailIntegrationRepository.save(integration);

                return newAccessToken;
            } else {
                System.err.println("⚠️ [GMAIL OAUTH REFRESH FAILED] HTTP " + response.statusCode() + " : " + response.body());
                if (response.body().contains("invalid_grant")) {
                    integration.setConnected(false);
                    integration.setStatus("EXPIRED");
                    gmailIntegrationRepository.save(integration);
                }
                return null;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Exception refreshing Gmail access token: " + e.getMessage());
            return null;
        }
    }
}
