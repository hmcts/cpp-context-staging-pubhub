package uk.gov.moj.cpp.staging.pubhub.event.service;

public enum SourceSystem {
    COMMON_PLATFORM("COMMON_PLATFORM"),
    LIST_ASSIST("LIST_ASSIST");

    private final String value;

    public String getValue() {
        return value;
    }

    SourceSystem(String value) {
        this.value = value;
    }
}
