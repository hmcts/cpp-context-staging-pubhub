package uk.gov.moj.cpp.stagingpubhub.domain;

import uk.gov.justice.staging.pubhub.schema.VenueContact;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"squid:S1067", "squid:S2384"})
public class CourtRoom implements Serializable {
    private static final long serialVersionUID = -5384779540784619225L;

    private final Integer courtRoomId;

    private final String courtRoomName;

    private final Integer courtRoomNumber;

    private final List<Session> session;

    private final VenueContact venueContact;

    public CourtRoom(final Integer courtRoomId, final String courtRoomName, final Integer courtRoomNumber, final List<Session> session, final VenueContact venueContact) {
        this.courtRoomId = courtRoomId;
        this.courtRoomName = courtRoomName;
        this.courtRoomNumber = courtRoomNumber;
        this.session = session;
        this.venueContact = venueContact;
    }

    public static Builder courtRoom() {
        return new CourtRoom.Builder();
    }

    public Integer getCourtRoomId() {
        return courtRoomId;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public Integer getCourtRoomNumber() {
        return courtRoomNumber;
    }

    public List<Session> getSession() {
        return session;
    }

    public VenueContact getVenueContact() {
        return venueContact;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CourtRoom that = (CourtRoom) obj;

        return java.util.Objects.equals(this.courtRoomId, that.courtRoomId) &&
                java.util.Objects.equals(this.courtRoomName, that.courtRoomName) &&
                java.util.Objects.equals(this.courtRoomNumber, that.courtRoomNumber) &&
                java.util.Objects.equals(this.session, that.session) &&
                java.util.Objects.equals(this.venueContact, that.venueContact);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtRoomId, courtRoomName, courtRoomNumber, session, venueContact);
    }

    @Override
    public String toString() {
        return "CourtRoom{" +
                "courtRoomId='" + courtRoomId + "'," +
                "courtRoomName='" + courtRoomName + "'," +
                "courtRoomNumber='" + courtRoomNumber + "'," +
                "session='" + session + "'," +
                "venueContact='" + venueContact + "'" +
                "}";
    }

    public static class Builder {
        private Integer courtRoomId;

        private String courtRoomName;

        private Integer courtRoomNumber;

        private List<Session> session;

        private VenueContact venueContact;

        public Builder withCourtRoomId(final Integer courtRoomId) {
            this.courtRoomId = courtRoomId;
            return this;
        }

        public Builder withCourtRoomName(final String courtRoomName) {
            this.courtRoomName = courtRoomName;
            return this;
        }

        public Builder withCourtRoomNumber(final Integer courtRoomNumber) {
            this.courtRoomNumber = courtRoomNumber;
            return this;
        }

        public Builder withSession(final List<Session> session) {
            this.session = session;
            return this;
        }

        public Builder withVenueContact(final VenueContact venueContact) {
            this.venueContact = venueContact;
            return this;
        }

        public Builder withValuesFrom(final CourtRoom courtRoom) {
            this.courtRoomId = courtRoom.getCourtRoomId();
            this.courtRoomName = courtRoom.getCourtRoomName();
            this.courtRoomNumber = courtRoom.getCourtRoomNumber();
            this.session = courtRoom.getSession();
            this.venueContact = courtRoom.getVenueContact();
            return this;
        }

        public CourtRoom build() {
            return new CourtRoom(courtRoomId, courtRoomName, courtRoomNumber, session, venueContact);
        }
    }
}
