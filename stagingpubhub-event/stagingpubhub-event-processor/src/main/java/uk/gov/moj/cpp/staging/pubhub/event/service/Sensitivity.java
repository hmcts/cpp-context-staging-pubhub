package uk.gov.moj.cpp.staging.pubhub.event.service;

public enum Sensitivity {

    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE"),
    CLASSIFIED("CLASSIFIED");

    private final String value;

    Sensitivity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
