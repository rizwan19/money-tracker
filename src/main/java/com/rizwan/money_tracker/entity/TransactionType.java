package com.rizwan.money_tracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionType {
    INCOME(0),
    EXPENSE(1);

    private final int value;

    TransactionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TransactionType from(Integer value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case 0 -> INCOME;
            case 1 -> EXPENSE;
            default -> throw new IllegalArgumentException("Unknown TransactionType value: " + value);
        };
    }

    @JsonCreator
    public static TransactionType fromValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return from(number.intValue());
        }

        String normalizedValue = value.toString().trim();
        if (normalizedValue.isEmpty()) {
            return null;
        }

        if (normalizedValue.matches("-?\\d+")) {
            return from(Integer.parseInt(normalizedValue));
        }

        return TransactionType.valueOf(normalizedValue.toUpperCase());
    }
}
