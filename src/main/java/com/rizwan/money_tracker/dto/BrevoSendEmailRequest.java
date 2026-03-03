package com.rizwan.money_tracker.dto;

import java.util.List;

public record BrevoSendEmailRequest(
        Sender sender,
        List<To> to,
        String subject,
        String htmlContent,
        String textContent
) {
    public record Sender(String email, String name) {}
    public record To(String email, String name) {}
}
