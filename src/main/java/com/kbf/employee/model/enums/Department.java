package com.kbf.employee.model.enums;


public enum Department {
    ADMINISTRATION("Administration"),
    FISHERY("Fishery"),
    POULTRY("Poultry"),
    RABBITRY("Rabbitry"),
    CONSTRUCTION("Construction"),
    CROPS("Crops"),
    LIVESTOCK("Livestock"),
    FARM_MANAGEMENT("Farm Management");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    @Override
    public String toString() {
        return this.name();
    }
}