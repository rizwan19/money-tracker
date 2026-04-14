package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.TransactionDto;
import com.rizwan.money_tracker.entity.Category;
import com.rizwan.money_tracker.entity.Profile;
import com.rizwan.money_tracker.entity.Transaction;
import com.rizwan.money_tracker.entity.TransactionType;
import com.rizwan.money_tracker.repository.CategoryRepository;
import com.rizwan.money_tracker.repository.TransactionRepository;
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
public class TransactionService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final ProfileService profileService;
    private final BrevoEmailService brevoEmailService;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public TransactionDto create(TransactionDto dto) {
        Profile profile = profileService.getCurrentProfile();
        Category category = findCategoryByIdAndProfileId(dto.getCategoryId(), profile.getId());
        validateTypeMatchesCategory(dto.getType(), category);

        Transaction transaction = Transaction.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .type(dto.getType())
                .profile(profile)
                .category(category)
                .build();
        return toDto(transactionRepository.save(transaction));
    }

    public TransactionDto update(Long id, TransactionDto dto) {
        Long profileId = profileService.getCurrentProfile().getId();
        Transaction existingTransaction = transactionRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        Category category = findCategoryByIdAndProfileId(dto.getCategoryId(), profileId);
        validateTypeMatchesCategory(dto.getType(), category);

        if (transactionRepository.existsByNameAndProfileIdAndTypeAndIdNot(dto.getName(), profileId, dto.getType(), id)) {
            throw new RuntimeException("Transaction with name '" + dto.getName() + "' already exists for this type.");
        }

        existingTransaction.setName(dto.getName());
        existingTransaction.setCategory(category);
        existingTransaction.setAmount(dto.getAmount());
        existingTransaction.setDate(dto.getDate());
        existingTransaction.setIcon(dto.getIcon());
        existingTransaction.setType(dto.getType());
        return toDto(transactionRepository.save(existingTransaction));
    }

    public void delete(Long id) {
        Profile profile = profileService.getCurrentProfile();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        transactionRepository.delete(transaction);
    }

    public List<TransactionDto> getCurrentMonthTransactions(TransactionType type) {
        Profile profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        return transactionRepository.findByProfileIdAndTypeAndDateBetween(profile.getId(), type, startDate, endDate)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<TransactionDto> getLatestFiveTransactions(TransactionType type) {
        Profile profile = profileService.getCurrentProfile();
        return transactionRepository.findTop5ByProfileIdAndTypeOrderByDateDesc(profile.getId(), type)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public BigDecimal getTotalByType(TransactionType type) {
        Profile profile = profileService.getCurrentProfile();
        BigDecimal total = transactionRepository.sumByProfileIdAndType(profile.getId(), type);
        return Objects.nonNull(total) ? total : BigDecimal.ZERO;
    }

    public List<TransactionDto> filterTransactions(TransactionType type, LocalDate startDate, LocalDate endDate, String term, Sort sort) {
        Profile profile = profileService.getCurrentProfile();
        String searchTerm = term != null ? term : "";
        List<Transaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByProfileIdAndTypeAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), type, startDate, endDate, searchTerm, sort);
        } else if (startDate != null) {
            transactions = transactionRepository.findByProfileIdAndTypeAndDateGreaterThanEqualAndNameContainingIgnoreCase(profile.getId(), type, startDate, searchTerm, sort);
        } else if (endDate != null) {
            transactions = transactionRepository.findByProfileIdAndTypeAndDateLessThanEqualAndNameContainingIgnoreCase(profile.getId(), type, endDate, searchTerm, sort);
        } else {
            transactions = transactionRepository.findByProfileIdAndTypeAndNameContainingIgnoreCase(profile.getId(), type, searchTerm, sort);
        }

        return transactions.stream().map(this::toDto).toList();
    }

    public List<TransactionDto> getTransactionsOnDate(Long profileId, LocalDate date, TransactionType type) {
        return transactionRepository.findByProfileIdAndTypeAndDate(profileId, type, date)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public void emailCurrentMonthTransactionDetails(TransactionType type) {
        Profile profile = profileService.getCurrentProfile();
        List<TransactionDto> transactions = getCurrentMonthTransactions(type).stream()
                .sorted((left, right) -> {
                    LocalDate rightDate = right.getDate() != null ? right.getDate() : LocalDate.MIN;
                    LocalDate leftDate = left.getDate() != null ? left.getDate() : LocalDate.MIN;
                    return rightDate.compareTo(leftDate);
                })
                .toList();

        YearMonth currentMonth = YearMonth.now();
        BigDecimal totalAmount = transactions.stream()
                .map(TransactionDto::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String monthLabel = currentMonth.format(MONTH_FORMATTER);
        String typeLabel = toLowerCaseLabel(type);
        String subjectLabel = toTitleCaseLabel(type);
        String subject = subjectLabel + " details for " + monthLabel;

        brevoEmailService.sendEmail(
                profile.getEmail(),
                profile.getFullName(),
                subject,
                buildTransactionEmailHtml(profile.getFullName(), monthLabel, typeLabel, totalAmount, transactions),
                buildTransactionEmailText(profile.getFullName(), monthLabel, typeLabel, totalAmount, transactions)
        );
    }

    private TransactionDto toDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .name(transaction.getName())
                .icon(transaction.getIcon())
                .amount(transaction.getAmount())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .date(transaction.getDate())
                .type(transaction.getType())
                .createdAt(transaction.getCreatedAt())
                .modifiedAt(transaction.getModifiedAt())
                .build();
    }

    private Category findCategoryByIdAndProfileId(Long categoryId, Long profileId) {
        return categoryRepository.findByIdAndProfileId(categoryId, profileId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
    }

    private void validateTypeMatchesCategory(TransactionType type, Category category) {
        if (type == null) {
            throw new RuntimeException("Transaction type is required");
        }
        if (category.getType() == null || !type.name().equalsIgnoreCase(category.getType())) {
            throw new RuntimeException("Transaction type must match category type");
        }
    }

    private String buildTransactionEmailHtml(String fullName, String monthLabel, String typeLabel, BigDecimal totalAmount, List<TransactionDto> transactions) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: Arial, sans-serif; color: #1f2937;\">")
                .append("<h2>").append(escapeHtml(toTitleCaseLabel(typeLabel))).append(" details for ").append(escapeHtml(monthLabel)).append("</h2>")
                .append("<p>Hello ").append(escapeHtml(defaultValue(fullName))).append(",</p>")
                .append("<p>Here is your ").append(escapeHtml(typeLabel)).append(" summary for ").append(escapeHtml(monthLabel)).append(".</p>")
                .append("<p><strong>Total ").append(escapeHtml(typeLabel)).append(":</strong> ").append(totalAmount.toPlainString()).append("</p>");

        if (transactions.isEmpty()) {
            html.append("<p>No ").append(escapeHtml(typeLabel)).append(" records were found for this month.</p>");
        } else {
            html.append("<table style=\"border-collapse: collapse; width: 100%;\">")
                    .append("<thead><tr>")
                    .append(tableHeader("Name"))
                    .append(tableHeader("Category"))
                    .append(tableHeader("Amount"))
                    .append(tableHeader("Date"))
                    .append("</tr></thead><tbody>");

            for (TransactionDto transaction : transactions) {
                html.append("<tr>")
                        .append(tableCell(defaultValue(transaction.getName())))
                        .append(tableCell(defaultValue(transaction.getCategoryName())))
                        .append(tableCell(transaction.getAmount() != null ? transaction.getAmount().toPlainString() : ""))
                        .append(tableCell(formatDate(transaction.getDate())))
                        .append("</tr>");
            }

            html.append("</tbody></table>");
        }

        html.append("<p style=\"margin-top: 16px;\">Generated by Money Tracker.</p>")
                .append("</body></html>");
        return html.toString();
    }

    private String buildTransactionEmailText(String fullName, String monthLabel, String typeLabel, BigDecimal totalAmount, List<TransactionDto> transactions) {
        StringBuilder text = new StringBuilder();
        text.append("Hello ").append(defaultValue(fullName)).append(",\n\n")
                .append("Here is your ").append(typeLabel).append(" summary for ").append(monthLabel).append(".\n")
                .append("Total ").append(typeLabel).append(": ").append(totalAmount.toPlainString()).append("\n\n");

        if (transactions.isEmpty()) {
            text.append("No ").append(typeLabel).append(" records were found for this month.\n");
        } else {
            for (TransactionDto transaction : transactions) {
                text.append("- ")
                        .append(defaultValue(transaction.getName()))
                        .append(" | ")
                        .append(defaultValue(transaction.getCategoryName()))
                        .append(" | ")
                        .append(transaction.getAmount() != null ? transaction.getAmount().toPlainString() : "")
                        .append(" | ")
                        .append(formatDate(transaction.getDate()))
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

    private String toLowerCaseLabel(TransactionType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }

    private String toTitleCaseLabel(TransactionType type) {
        return toTitleCaseLabel(toLowerCaseLabel(type));
    }

    private String toTitleCaseLabel(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
