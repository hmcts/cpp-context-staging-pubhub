package uk.gov.moj.cpp.staging.pubhub.event.service;

public enum PartyType {

    ACCUSED("ACCUSED"),
    PROSECUTOR("PROSECUTOR");

    private final String value;

    PartyType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
