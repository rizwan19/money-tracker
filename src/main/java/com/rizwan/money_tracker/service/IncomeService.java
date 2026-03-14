package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.IncomeDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Income;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.repository.CategoryRepository;
import com.rizwan.money_tracker.repository.IncomeRepository;
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
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;
    private final BrevoEmailService brevoEmailService;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public IncomeDto addIncome(IncomeDto dto) {
        Profile profile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Income income = toEntity(dto, profile, category);
        return toDto(incomeRepository.save(income));
    }

    private Income toEntity(IncomeDto dto, Profile profile, Category category) {
        return Income.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDto toDto(Income income) {
        return IncomeDto.builder()
                .id(income.getId())
                .name(income.getName())
                .icon(income.getIcon())
                .amount(income.getAmount())
                .categoryId(income.getCategory().getId())
                .categoryName(income.getCategory().getName())
                .date(income.getDate())
                .createdAt(income.getCreatedAt())
                .modifiedAt(income.getModifiedAt())
                .build();
    }

    public List<IncomeDto> getCurrentMonthIncomes() {
        Profile profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth((1));
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<Income> incomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return incomes.stream().map(this::toDto).toList();
    }

    public void deleteIncome(Long id) {
        Profile profile = profileService.getCurrentProfile();
        Income income = incomeRepository.findById(id).orElseThrow(() -> new RuntimeException("Income not found"));

        if (!income.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        incomeRepository.delete(income);
    }

    public List<IncomeDto> getLatestFiveIncomes() {
        Profile profile = profileService.getCurrentProfile();
        List<Income> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return incomes.stream().map(this::toDto).toList();
    }

    public BigDecimal getTotalIncome() {
        Profile profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return Objects.nonNull(total) ? total : BigDecimal.ZERO;
    }

    public List<IncomeDto> filterIncomes(LocalDate startDate, LocalDate endDate, String term, Sort sort) {
        Profile profile = profileService.getCurrentProfile();
        List<Income> incomes;

        if (startDate != null && endDate != null) {
            incomes = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, term, sort);
        } else if (startDate != null) {
            incomes = incomeRepository.findByProfileIdAndDateGreaterThanEqualAndNameContainingIgnoreCase(profile.getId(), startDate, term, sort);
        } else if (endDate != null) {
            incomes = incomeRepository.findByProfileIdAndDateLessThanEqualAndNameContainingIgnoreCase(profile.getId(), endDate, term, sort);
        } else {
            incomes = incomeRepository.findByProfileIdAndNameContainingIgnoreCase(profile.getId(), term, sort);
        }

        return incomes.stream().map(this::toDto).toList();
    }

    public void emailCurrentMonthIncomeDetails() {
        Profile profile = profileService.getCurrentProfile();
        List<IncomeDto> incomes = getCurrentMonthIncomes().stream()
                .sorted((left, right) -> {
                    LocalDate rightDate = right.getDate() != null ? right.getDate() : LocalDate.MIN;
                    LocalDate leftDate = left.getDate() != null ? left.getDate() : LocalDate.MIN;
                    return rightDate.compareTo(leftDate);
                })
                .toList();

        YearMonth currentMonth = YearMonth.now();
        BigDecimal totalIncome = incomes.stream()
                .map(IncomeDto::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String monthLabel = currentMonth.format(MONTH_FORMATTER);
        String subject = "Income details for " + monthLabel;

        brevoEmailService.sendEmail(
                profile.getEmail(),
                profile.getFullName(),
                subject,
                buildIncomeEmailHtml(profile.getFullName(), monthLabel, totalIncome, incomes),
                buildIncomeEmailText(profile.getFullName(), monthLabel, totalIncome, incomes)
        );
    }

    private String defaultValue(String value) {
        return value != null ? value : "";
    }

    private String buildIncomeEmailHtml(String fullName, String monthLabel, BigDecimal totalIncome, List<IncomeDto> incomes) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: Arial, sans-serif; color: #1f2937;\">")
                .append("<h2>Income details for ").append(escapeHtml(monthLabel)).append("</h2>")
                .append("<p>Hello ").append(escapeHtml(defaultValue(fullName))).append(",</p>")
                .append("<p>Here is your income summary for ").append(escapeHtml(monthLabel)).append(".</p>")
                .append("<p><strong>Total income:</strong> ").append(totalIncome.toPlainString()).append("</p>");

        if (incomes.isEmpty()) {
            html.append("<p>No income records were found for this month.</p>");
        } else {
            html.append("<table style=\"border-collapse: collapse; width: 100%;\">")
                    .append("<thead><tr>")
                    .append(tableHeader("Name"))
                    .append(tableHeader("Category"))
                    .append(tableHeader("Amount"))
                    .append(tableHeader("Date"))
                    .append("</tr></thead><tbody>");

            for (IncomeDto income : incomes) {
                html.append("<tr>")
                        .append(tableCell(defaultValue(income.getName())))
                        .append(tableCell(defaultValue(income.getCategoryName())))
                        .append(tableCell(income.getAmount() != null ? income.getAmount().toPlainString() : ""))
                        .append(tableCell(formatDate(income.getDate())))
                        .append("</tr>");
            }

            html.append("</tbody></table>");
        }

        html.append("<p style=\"margin-top: 16px;\">Generated by Money Tracker.</p>")
                .append("</body></html>");
        return html.toString();
    }

    private String buildIncomeEmailText(String fullName, String monthLabel, BigDecimal totalIncome, List<IncomeDto> incomes) {
        StringBuilder text = new StringBuilder();
        text.append("Hello ").append(defaultValue(fullName)).append(",\n\n")
                .append("Here is your income summary for ").append(monthLabel).append(".\n")
                .append("Total income: ").append(totalIncome.toPlainString()).append("\n\n");

        if (incomes.isEmpty()) {
            text.append("No income records were found for this month.\n");
        } else {
            for (IncomeDto income : incomes) {
                text.append("- ")
                        .append(defaultValue(income.getName()))
                        .append(" | ")
                        .append(defaultValue(income.getCategoryName()))
                        .append(" | ")
                        .append(income.getAmount() != null ? income.getAmount().toPlainString() : "")
                        .append(" | ")
                        .append(formatDate(income.getDate()))
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
}
