package uk.gov.moj.cpp.staging.pubhub.event.service;

public enum ArtefactType {
    LIST("LIST"),
    LCSU("LCSU"),
    JUDGEMENTS_AND_OUTCOMES("JUDGEMENTS_AND_OUTCOMES"),
    GENERAL_PUBLICATIONLIST("GENERAL_PUBLICATIONLIST");

    public String getValue() {
        return value;
    }

    private final String value;

    ArtefactType(String value) {
        this.value = value;
    }
}
