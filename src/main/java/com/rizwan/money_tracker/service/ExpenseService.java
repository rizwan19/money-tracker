package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.ExpenseDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Expense;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.CategoryRepository;
import com.rizwan.money_tracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;
    private final BrevoEmailService brevoEmailService;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;


    public ExpenseDto addExpense(ExpenseDto dto) {
        Profile profile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Expense expense = toEntity(dto, profile, category);
        return toDto(expenseRepository.save(expense));
    }

    public List<ExpenseDto> getCurrentMonthExpenses() {
        Profile profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth((1));
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<Expense> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return expenses.stream().map(this::toDto).toList();
    }

    private Expense toEntity(ExpenseDto dto, Profile profile, Category category) {
        return Expense.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDto toDto(Expense expense) {
        return ExpenseDto.builder()
                .id(expense.getId())
                .name(expense.getName())
                .icon(expense.getIcon())
                .amount(expense.getAmount())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .modifiedAt(expense.getModifiedAt())
                .build();
    }

    public void deleteExpense(Long id) {
        Profile profile = profileService.getCurrentProfile();
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        expenseRepository.delete(expense);
    }

    public List<ExpenseDto> getLatestFiveExpenses() {
        Profile profile = profileService.getCurrentProfile();
        List<Expense> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenses.stream().map(this::toDto).toList();
    }

    public BigDecimal getTotalExpense() {
        Profile profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return Objects.nonNull(total) ? total : BigDecimal.ZERO;
    }

    public List<ExpenseDto> filterExpenses(LocalDate startDate, LocalDate endDate, String term, Sort sort) {
        Profile profile = profileService.getCurrentProfile();
        List<Expense> expenses;

        if (startDate != null && endDate != null) {
            expenses = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, term, sort);
        } else if (startDate != null) {
            expenses = expenseRepository.findByProfileIdAndDateGreaterThanEqualAndNameContainingIgnoreCase(profile.getId(), startDate, term, sort);
        } else if (endDate != null) {
            expenses = expenseRepository.findByProfileIdAndDateLessThanEqualAndNameContainingIgnoreCase(profile.getId(), endDate, term, sort);
        } else {
            expenses = expenseRepository.findByProfileIdAndNameContainingIgnoreCase(profile.getId(), term, sort);
        }

        return expenses.stream().map(this::toDto).toList();
    }

    public List<ExpenseDto> getExpensesOnDate(Long profileId, LocalDate date) {
        List<Expense> expenses = expenseRepository.findByProfileIdAndDate(profileId, date);
        return expenses.stream().map(this::toDto).toList();
    }

    public void emailCurrentMonthExpenseDetails() {
        Profile profile = profileService.getCurrentProfile();
        List<ExpenseDto> expenses = getCurrentMonthExpenses().stream()
                .sorted((left, right) -> {
                    LocalDate rightDate = right.getDate() != null ? right.getDate() : LocalDate.MIN;
                    LocalDate leftDate = left.getDate() != null ? left.getDate() : LocalDate.MIN;
                    return rightDate.compareTo(leftDate);
                })
                .toList();

        YearMonth currentMonth = YearMonth.now();
        BigDecimal totalIncome = expenses.stream()
                .map(ExpenseDto::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String monthLabel = currentMonth.format(MONTH_FORMATTER);
        String subject = "Expense details for " + monthLabel;

        brevoEmailService.sendEmail(
                profile.getEmail(),
                profile.getFullName(),
                subject,
                buildExpenseEmailHtml(profile.getFullName(), monthLabel, totalIncome, expenses),
                buildExpenseEmailText(profile.getFullName(), monthLabel, totalIncome, expenses)
        );
    }

    private String buildExpenseEmailHtml(String fullName, String monthLabel, BigDecimal totalIncome, List<ExpenseDto> expenses) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: Arial, sans-serif; color: #1f2937;\">")
                .append("<h2>Expense details for ").append(escapeHtml(monthLabel)).append("</h2>")
                .append("<p>Hello ").append(escapeHtml(defaultValue(fullName))).append(",</p>")
                .append("<p>Here is your expense summary for ").append(escapeHtml(monthLabel)).append(".</p>")
                .append("<p><strong>Total expense:</strong> ").append(totalIncome.toPlainString()).append("</p>");

        if (expenses.isEmpty()) {
            html.append("<p>No expense records were found for this month.</p>");
        } else {
            html.append("<table style=\"border-collapse: collapse; width: 100%;\">")
                    .append("<thead><tr>")
                    .append(tableHeader("Name"))
                    .append(tableHeader("Category"))
                    .append(tableHeader("Amount"))
                    .append(tableHeader("Date"))
                    .append("</tr></thead><tbody>");

            for (ExpenseDto expense : expenses) {
                html.append("<tr>")
                        .append(tableCell(defaultValue(expense.getName())))
                        .append(tableCell(defaultValue(expense.getCategoryName())))
                        .append(tableCell(expense.getAmount() != null ? expense.getAmount().toPlainString() : ""))
                        .append(tableCell(formatDate(expense.getDate())))
                        .append("</tr>");
            }

            html.append("</tbody></table>");
        }

        html.append("<p style=\"margin-top: 16px;\">Generated by Money Tracker.</p>")
                .append("</body></html>");
        return html.toString();
    }

    private String buildExpenseEmailText(String fullName, String monthLabel, BigDecimal totalIncome, List<ExpenseDto> expenses) {
        StringBuilder text = new StringBuilder();
        text.append("Hello ").append(defaultValue(fullName)).append(",\n\n")
                .append("Here is your expense summary for ").append(monthLabel).append(".\n")
                .append("Total expense: ").append(totalIncome.toPlainString()).append("\n\n");

        if (expenses.isEmpty()) {
            text.append("No expense records were found for this month.\n");
        } else {
            for (ExpenseDto expense : expenses) {
                text.append("- ")
                        .append(defaultValue(expense.getName()))
                        .append(" | ")
                        .append(defaultValue(expense.getCategoryName()))
                        .append(" | ")
                        .append(expense.getAmount() != null ? expense.getAmount().toPlainString() : "")
                        .append(" | ")
                        .append(formatDate(expense.getDate()))
                        .append("\n");
            }
        }

        text.append("\nGenerated by Money Tracker.");
        return text.toString();
    }

    private String tableHeader(String value) {
        return "<th style=\"border: 1px solid #d1d5db; padding: 8px; text-align: left; background: #f3f4f6;\">"
                + escapeHtml(value) + "</th>";
    }

    private String tableCell(String value) {
        return "<td style=\"border: 1px solid #d1d5db; padding: 8px;\">"
                + escapeHtml(value) + "</td>";
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    private String escapeHtml(String value) {
        return defaultValue(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String defaultValue(String value) {
        return value != null ? value : "";
    }

    public ExpenseDto updateExpense(ExpenseDto dto) {
        Long profileId = profileService.getCurrentProfile().getId();
        Expense existingExpense = expenseRepository.findByIdAndProfileId(dto.getId(), profileId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + dto.getId()));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));

        if (expenseRepository.existsByNameAndProfileIdAndIdNot(dto.getName(), profileId, dto.getId())) {
            throw new RuntimeException("Expense with name '" + dto.getName() + "' already exists for this profile.");
        }
        existingExpense.setName(dto.getName());
        existingExpense.setCategory(category);
        existingExpense.setAmount(dto.getAmount());
        existingExpense.setDate(dto.getDate());
        existingExpense.setIcon(dto.getIcon());
        return toDto(expenseRepository.save(existingExpense));
    }

}
