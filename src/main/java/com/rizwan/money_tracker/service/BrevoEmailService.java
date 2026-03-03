package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.BrevoSendEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class BrevoEmailService {

    private final RestClient restClient;

    @Value("${mail.from}")
    private String fromEmail;

    @Value("${mail.from.name}")
    private String fromName;

    public BrevoEmailService(
            @Value("${brevo.api.url}") String apiUrl,
            @Value("${brevo.api.key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("api-key", apiKey)
                .defaultHeader("accept", "application/json")
                .build();
    }

    public void sendEmail(String toEmail, String toName, String subject, String html, String text) {
        BrevoSendEmailRequest body = new BrevoSendEmailRequest(
                new BrevoSendEmailRequest.Sender(fromEmail, fromName),
                List.of(new BrevoSendEmailRequest.To(toEmail, toName)),
                subject,
                html,
                text
        );

        restClient.post()
                .uri("/smtp/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
