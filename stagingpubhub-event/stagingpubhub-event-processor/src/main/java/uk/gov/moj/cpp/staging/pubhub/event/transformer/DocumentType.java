package uk.gov.moj.cpp.staging.pubhub.event.transformer;

public enum DocumentType {
    MAGS_PUBLIC_LIST("Magistrates Public List"),
    MAGS_STANDARD_LIST_ENGLISH("Magistrates Standard List English"),
    CROWN_LCSU("Live Case Updates"),
    SJP_PUBLIC_LIST("SJP Pending list"),
    SJP_PRESS_LIST("SJP Press list");

    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
