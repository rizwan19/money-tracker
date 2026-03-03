package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.ExpenseDto;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final ExpenseService expenseService;
    private final BrevoEmailService brevoEmailService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "IST") // Every day at 11 PM
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder");
        List<Profile> profiles = profileRepository.findAll();

        for(Profile profile: profiles) {
            String body = "Dear " + profile.getFullName() + ",\n\n" +
                    "This is a friendly reminder to log your daily income and expenses in the Money Tracker app.\n\n" +
                    "You can log your transactions here: " + frontendUrl + "\n\n" +
                    "Best regards,\n" +
                    "Money Tracker Team";
            brevoEmailService.sendEmail(profile.getEmail(), profile.getFullName(), "Daily Income & Expense Reminder", body, "Thanks.");
        }
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "IST")
    public void sendDailyExpenseSummary() {
        log.info("Job started: sendDailyExpenseSummary");
        List<Profile> profiles = profileRepository.findAll();

        for(Profile profile: profiles) {
            List<ExpenseDto> todaysExpenses = expenseService.getExpensesOnDate(profile.getId(), LocalDateTime.now());

            StringBuilder table = new StringBuilder();
            table.append("<table>");
            table.append("<tr><th>Name</th><th>Amount</th><th>Category</th><th>Date</th></tr>");

            if (!todaysExpenses.isEmpty()) {
                for (ExpenseDto expense : todaysExpenses) {
                    table.append("<tr>")
                            .append("<td>").append(expense.getName()).append("</td>")
                            .append("<td>").append(expense.getAmount()).append("</td>")
                            .append("<td>").append(expense.getCategoryName()).append("</td>")
                            .append("<td>").append(expense.getDate().toLocalTime().withSecond(0).withNano(0)).append("</td>")
                            .append("</tr>");
                }
                table.append("</table>");
                String body = "Dear " + profile.getFullName() + ",\n\n" +
                        "Here is a summary of your expenses for today:\n\n" +
                        table + "\n\n" +
                        "You can view more details in the Money Tracker app: " + frontendUrl + "\n\n" +
                        "Best regards,\n" +
                        "Money Tracker Team";
                brevoEmailService.sendEmail(profile.getEmail(), profile.getFullName(), "Daily Expense Summary", body, "Thanks");
                log.info("Job completed: sendDailyExpenseSummary");
            }
        }
    }
}
