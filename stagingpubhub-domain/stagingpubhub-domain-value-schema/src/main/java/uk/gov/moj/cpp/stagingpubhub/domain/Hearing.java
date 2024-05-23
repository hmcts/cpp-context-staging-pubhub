package uk.gov.moj.cpp.stagingpubhub.domain;

import uk.gov.justice.staging.pubhub.schema.Cases;
import uk.gov.justice.staging.pubhub.schema.Lcsu;
import uk.gov.justice.staging.pubhub.schema.Offence;
import uk.gov.justice.staging.pubhub.schema.Party;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)

@SuppressWarnings({"squid:S1067", "squid:S2384"})
public class Hearing implements Serializable {
    private static final long serialVersionUID = -5384779540784619225L;

    @JsonProperty(value = "case")
    private final List<Cases> cases;

    private final String hearingDescription;

    private final String hearingId;

    private final Integer hearingSequence;

    private final String hearingType;

    private final Lcsu lcsu;

    private final String listNote;

    private final List<Offence> offence;

    private final List<Party> party;

    public Hearing(final List<Cases> cases, final String hearingDescription, final String hearingId, final Integer hearingSequence, final String hearingType, final Lcsu lcsu, final String listNote, final List<Offence> offence, final List<Party> party) {
        this.cases = cases;
        this.hearingDescription = hearingDescription;
        this.hearingId = hearingId;
        this.hearingSequence = hearingSequence;
        this.hearingType = hearingType;
        this.lcsu = lcsu;
        this.listNote = listNote;
        this.offence = offence;
        this.party = party;
    }

    public static Builder hearing() {
        return new Hearing.Builder();
    }

    @JsonProperty(value = "case")
    public List<Cases> getCases() {
        return cases;
    }

    public String getHearingDescription() {
        return hearingDescription;
    }

    public String getHearingId() {
        return hearingId;
    }

    public Integer getHearingSequence() {
        return hearingSequence;
    }

    public String getHearingType() {
        return hearingType;
    }

    public Lcsu getLcsu() {
        return lcsu;
    }

    public String getListNote() {
        return listNote;
    }

    public List<Offence> getOffence() {
        return offence;
    }

    public List<Party> getParty() {
        return party;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Hearing that = (Hearing) obj;

        return java.util.Objects.equals(this.cases, that.cases) &&
                java.util.Objects.equals(this.hearingDescription, that.hearingDescription) &&
                java.util.Objects.equals(this.hearingId, that.hearingId) &&
                java.util.Objects.equals(this.hearingSequence, that.hearingSequence) &&
                java.util.Objects.equals(this.hearingType, that.hearingType) &&
                java.util.Objects.equals(this.lcsu, that.lcsu) &&
                java.util.Objects.equals(this.listNote, that.listNote) &&
                java.util.Objects.equals(this.offence, that.offence) &&
                java.util.Objects.equals(this.party, that.party);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(cases, hearingDescription, hearingId, hearingSequence, hearingType, lcsu, listNote, offence, party);
    }

    @Override
    public String toString() {
        return "Hearing{" +
                "cases='" + cases + "'," +
                "hearingDescription='" + hearingDescription + "'," +
                "hearingId='" + hearingId + "'," +
                "hearingSequence='" + hearingSequence + "'," +
                "hearingType='" + hearingType + "'," +
                "lcsu='" + lcsu + "'," +
                "listNote='" + listNote + "'," +
                "offence='" + offence + "'," +
                "party='" + party + "'" +
                "}";
    }

    public static class Builder {
        private List<Cases> cases;

        private String hearingDescription;

        private String hearingId;

        private Integer hearingSequence;

        private String hearingType;

        private Lcsu lcsu;

        private String listNote;

        private List<Offence> offence;

        private List<Party> party;

        public Builder withCases(final List<Cases> cases) {
            this.cases = cases;
            return this;
        }

        public Builder withHearingDescription(final String hearingDescription) {
            this.hearingDescription = hearingDescription;
            return this;
        }

        public Builder withHearingId(final String hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withHearingSequence(final Integer hearingSequence) {
            this.hearingSequence = hearingSequence;
            return this;
        }

        public Builder withHearingType(final String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public Builder withLcsu(final Lcsu lcsu) {
            this.lcsu = lcsu;
            return this;
        }

        public Builder withListNote(final String listNote) {
            this.listNote = listNote;
            return this;
        }

        public Builder withOffence(final List<Offence> offence) {
            this.offence = offence;
            return this;
        }

        public Builder withParty(final List<Party> party) {
            this.party = party;
            return this;
        }

        public Builder withValuesFrom(final Hearing hearing) {
            this.cases = hearing.getCases();
            this.hearingDescription = hearing.getHearingDescription();
            this.hearingId = hearing.getHearingId();
            this.hearingSequence = hearing.getHearingSequence();
            this.hearingType = hearing.getHearingType();
            this.lcsu = hearing.getLcsu();
            this.listNote = hearing.getListNote();
            this.offence = hearing.getOffence();
            this.party = hearing.getParty();
            return this;
        }

        public Hearing build() {
            return new Hearing(cases, hearingDescription, hearingId, hearingSequence, hearingType, lcsu, listNote, offence, party);
        }
    }
}
