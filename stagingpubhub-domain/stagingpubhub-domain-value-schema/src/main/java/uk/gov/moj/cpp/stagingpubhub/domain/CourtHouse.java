package uk.gov.moj.cpp.stagingpubhub.domain;

import uk.gov.justice.staging.pubhub.schema.CourtHouseType;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"squid:S1067", "squid:S2384"})
public class CourtHouse implements Serializable {
    private static final long serialVersionUID = -5384779540784619225L;

    private final String courtHouseCode;

    private final String courtHouseDescription;

    private final String courtHouseName;

    private final CourtHouseType courtHouseType;

    private final List<CourtRoom> courtRoom;

    public CourtHouse(final String courtHouseCode, final String courtHouseDescription, final String courtHouseName, final CourtHouseType courtHouseType, final List<CourtRoom> courtRoom) {
        this.courtHouseCode = courtHouseCode;
        this.courtHouseDescription = courtHouseDescription;
        this.courtHouseName = courtHouseName;
        this.courtHouseType = courtHouseType;
        this.courtRoom = courtRoom;
    }

    public static Builder courtHouse() {
        return new CourtHouse.Builder();
    }

    public String getCourtHouseCode() {
        return courtHouseCode;
    }

    public String getCourtHouseDescription() {
        return courtHouseDescription;
    }

    public String getCourtHouseName() {
        return courtHouseName;
    }

    public CourtHouseType getCourtHouseType() {
        return courtHouseType;
    }

    public List<CourtRoom> getCourtRoom() {
        return courtRoom;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CourtHouse that = (CourtHouse) obj;

        return java.util.Objects.equals(this.courtHouseCode, that.courtHouseCode) &&
                java.util.Objects.equals(this.courtHouseDescription, that.courtHouseDescription) &&
                java.util.Objects.equals(this.courtHouseName, that.courtHouseName) &&
                java.util.Objects.equals(this.courtHouseType, that.courtHouseType) &&
                java.util.Objects.equals(this.courtRoom, that.courtRoom);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtHouseCode, courtHouseDescription, courtHouseName, courtHouseType, courtRoom);
    }

    @Override
    public String toString() {
        return "CourtHouse{" +
                "courtHouseCode='" + courtHouseCode + "'," +
                "courtHouseDescription='" + courtHouseDescription + "'," +
                "courtHouseName='" + courtHouseName + "'," +
                "courtHouseType='" + courtHouseType + "'," +
                "courtRoom='" + courtRoom + "'" +
                "}";
    }

    public static class Builder {
        private String courtHouseCode;

        private String courtHouseDescription;

        private String courtHouseName;

        private CourtHouseType courtHouseType;

        private List<CourtRoom> courtRoom;

        public Builder withCourtHouseCode(final String courtHouseCode) {
            this.courtHouseCode = courtHouseCode;
            return this;
        }

        public Builder withCourtHouseDescription(final String courtHouseDescription) {
            this.courtHouseDescription = courtHouseDescription;
            return this;
        }

        public Builder withCourtHouseName(final String courtHouseName) {
            this.courtHouseName = courtHouseName;
            return this;
        }

        public Builder withCourtHouseType(final CourtHouseType courtHouseType) {
            this.courtHouseType = courtHouseType;
            return this;
        }

        public Builder withCourtRoom(final List<CourtRoom> courtRoom) {
            this.courtRoom = courtRoom;
            return this;
        }

        public Builder withValuesFrom(final CourtHouse courtHouse) {
            this.courtHouseCode = courtHouse.getCourtHouseCode();
            this.courtHouseDescription = courtHouse.getCourtHouseDescription();
            this.courtHouseName = courtHouse.getCourtHouseName();
            this.courtHouseType = courtHouse.getCourtHouseType();
            this.courtRoom = courtHouse.getCourtRoom();
            return this;
        }

        public CourtHouse build() {
            return new CourtHouse(courtHouseCode, courtHouseDescription, courtHouseName, courtHouseType, courtRoom);
        }
    }
}
