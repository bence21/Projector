package com.bence.projector.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewedWordStatusDTO {
    UNREVIEWED("UNREVIEWED"),
    REVIEWED_GOOD("REVIEWED_GOOD"),
    CONTEXT_SPECIFIC("CONTEXT_SPECIFIC"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED"),
    BANNED("BANNED"),
    AUTO_ACCEPTED_FROM_PUBLIC("AUTO_ACCEPTED_FROM_PUBLIC");

    private final String value;

    ReviewedWordStatusDTO(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ReviewedWordStatusDTO fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ReviewedWordStatusDTO status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewedWordStatusDTO: " + value);
    }
}
