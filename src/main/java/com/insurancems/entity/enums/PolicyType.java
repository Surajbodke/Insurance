package com.insurancems.entity.enums;

public enum PolicyType {
    HEALTH("Health Insurance"),
    AUTO("Auto Insurance"),
    LIFE("Life Insurance"),
    HOME("Home Insurance"),
    TRAVEL("Travel Insurance"),
    BUSINESS("Business Insurance");

    private final String displayName;

    PolicyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 