package com.bence.projector.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WordStatus {
    GOOD("good"),
    UNREVIEWED("unreviewed"),
    BANNED("banned"),
    REJECTED("rejected");

    private final String value;

    WordStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WordStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (WordStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown WordStatus: " + value);
    }
}
