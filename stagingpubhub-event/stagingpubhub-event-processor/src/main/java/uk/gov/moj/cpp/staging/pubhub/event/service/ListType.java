package uk.gov.moj.cpp.staging.pubhub.event.service;

public enum ListType {

    SJP_PUBLIC_LIST("SJP_PUBLIC_LIST"),
    SJP_PRESS_LIST("SJP_PRESS_LIST"),
    CROWN_DAILY_LIST("CROWN_DAILY_LIST"),
    CROWN_LCSU("CROWN_LCSU"),
    CROWN_FIRM_LIST("CROWN_FIRM_LIST"),
    CROWN_WARNED_LIST("CROWN_WARNED_LIST"),
    MAGS_PUBLIC_LIST("MAGS_PUBLIC_LIST"),
    MAGS_STANDARD_LIST("MAGS_STANDARD_LIST"),
    CIVIL_DAILY_CAUSE_LIST("CIVIL_DAILY_CAUSE_LIST"),
    FAMILY_DAILY_CAUSE_LIST("FAMILY_DAILY_CAUSE_LIST");

    public String getValue() {
        return value;
    }

    private final String value;

    ListType(String value) {
        this.value = value;
    }
}
