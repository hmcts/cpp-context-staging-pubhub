package uk.gov.moj.cpp.stagingpubhub.domain;

import uk.gov.justice.staging.pubhub.schema.Document;
import uk.gov.justice.staging.pubhub.schema.Venue;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"squid:S1067", "squid:S2384"})
public class PubhubMaster implements Serializable {
    private static final long serialVersionUID = -5384779540784619225L;

    private final List<CourtLists> courtLists;

    private final Document document;

    private final String schemaLocation;

    private final Venue venue;

    public PubhubMaster(final List<CourtLists> courtLists, final Document document, final String schemaLocation, final Venue venue) {
        this.courtLists = courtLists;
        this.document = document;
        this.schemaLocation = schemaLocation;
        this.venue = venue;
    }

    public static Builder pubhubMaster() {
        return new PubhubMaster.Builder();
    }

    public List<CourtLists> getCourtLists() {
        return courtLists;
    }

    public Document getDocument() {
        return document;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public Venue getVenue() {
        return venue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PubhubMaster that = (PubhubMaster) obj;

        return java.util.Objects.equals(this.courtLists, that.courtLists) &&
                java.util.Objects.equals(this.document, that.document) &&
                java.util.Objects.equals(this.schemaLocation, that.schemaLocation) &&
                java.util.Objects.equals(this.venue, that.venue);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtLists, document, schemaLocation, venue);
    }

    @Override
    public String toString() {
        return "PubhubMaster{" +
                "courtLists='" + courtLists + "'," +
                "document='" + document + "'," +
                "schemaLocation='" + schemaLocation + "'," +
                "venue='" + venue + "'" +
                "}";
    }

    public static class Builder {
        private List<CourtLists> courtLists;

        private Document document;

        private String schemaLocation;

        private Venue venue;

        public Builder withCourtLists(final List<CourtLists> courtLists) {
            this.courtLists = courtLists;
            return this;
        }

        public Builder withDocument(final Document document) {
            this.document = document;
            return this;
        }

        public Builder withSchemaLocation(final String schemaLocation) {
            this.schemaLocation = schemaLocation;
            return this;
        }

        public Builder withVenue(final Venue venue) {
            this.venue = venue;
            return this;
        }

        public Builder withValuesFrom(final PubhubMaster pubhubMaster) {
            this.courtLists = pubhubMaster.getCourtLists();
            this.document = pubhubMaster.getDocument();
            this.schemaLocation = pubhubMaster.getSchemaLocation();
            this.venue = pubhubMaster.getVenue();
            return this;
        }

        public PubhubMaster build() {
            return new PubhubMaster(courtLists, document, schemaLocation, venue);
        }
    }
}
