package uk.gov.moj.cpp.stagingpubhub.domain;

import java.io.Serializable;

@SuppressWarnings({"squid:S1067", "squid:S2384"})
public class CourtLists implements Serializable {
    private static final long serialVersionUID = -5384779540784619225L;

    private final CourtHouse courtHouse;

    public CourtLists(final CourtHouse courtHouse) {
        this.courtHouse = courtHouse;
    }

    public static Builder courtLists() {
        return new CourtLists.Builder();
    }

    public CourtHouse getCourtHouse() {
        return courtHouse;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CourtLists that = (CourtLists) obj;

        return java.util.Objects.equals(this.courtHouse, that.courtHouse);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtHouse);
    }

    @Override
    public String toString() {
        return "CourtLists{" +
                "courtHouse='" + courtHouse + "'" +
                "}";
    }

    public static class Builder {
        private CourtHouse courtHouse;

        public Builder withCourtHouse(final CourtHouse courtHouse) {
            this.courtHouse = courtHouse;
            return this;
        }

        public Builder withValuesFrom(final CourtLists courtLists) {
            this.courtHouse = courtLists.getCourtHouse();
            return this;
        }

        public CourtLists build() {
            return new CourtLists(courtHouse);
        }
    }
}
