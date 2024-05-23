package uk.gov.moj.cpp.stagingpubhub.domain;

import uk.gov.justice.staging.pubhub.schema.Judiciary;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"squid:S1067", "squid:S2384"})
public class Sittings implements Serializable {
    private static final long serialVersionUID = -5384779540784619225L;

    private final List<String> channel;

    private final List<Hearing> hearing;

    private final List<Judiciary> judiciary;

    private final String sittingEnd;

    private final Integer sittingSequence;

    private final String sittingStart;

    public Sittings(final List<String> channel, final List<Hearing> hearing, final List<Judiciary> judiciary, final String sittingEnd, final Integer sittingSequence, final String sittingStart) {
        this.channel = channel;
        this.hearing = hearing;
        this.judiciary = judiciary;
        this.sittingEnd = sittingEnd;
        this.sittingSequence = sittingSequence;
        this.sittingStart = sittingStart;
    }

    public static Builder sittings() {
        return new Sittings.Builder();
    }

    public List<String> getChannel() {
        return channel;
    }

    public List<Hearing> getHearing() {
        return hearing;
    }

    public List<Judiciary> getJudiciary() {
        return judiciary;
    }

    public String getSittingEnd() {
        return sittingEnd;
    }

    public Integer getSittingSequence() {
        return sittingSequence;
    }

    public String getSittingStart() {
        return sittingStart;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Sittings that = (Sittings) obj;

        return java.util.Objects.equals(this.channel, that.channel) &&
                java.util.Objects.equals(this.hearing, that.hearing) &&
                java.util.Objects.equals(this.judiciary, that.judiciary) &&
                java.util.Objects.equals(this.sittingEnd, that.sittingEnd) &&
                java.util.Objects.equals(this.sittingSequence, that.sittingSequence) &&
                java.util.Objects.equals(this.sittingStart, that.sittingStart);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(channel, hearing, judiciary, sittingEnd, sittingSequence, sittingStart);
    }

    @Override
    public String toString() {
        return "Sittings{" +
                "channel='" + channel + "'," +
                "hearing='" + hearing + "'," +
                "judiciary='" + judiciary + "'," +
                "sittingEnd='" + sittingEnd + "'," +
                "sittingSequence='" + sittingSequence + "'," +
                "sittingStart='" + sittingStart + "'" +
                "}";
    }

    public static class Builder {
        private List<String> channel;

        private List<Hearing> hearing;

        private List<Judiciary> judiciary;

        private String sittingEnd;

        private Integer sittingSequence;

        private String sittingStart;

        public Builder withChannel(final List<String> channel) {
            this.channel = channel;
            return this;
        }

        public Builder withHearing(final List<Hearing> hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withJudiciary(final List<Judiciary> judiciary) {
            this.judiciary = judiciary;
            return this;
        }

        public Builder withSittingEnd(final String sittingEnd) {
            this.sittingEnd = sittingEnd;
            return this;
        }

        public Builder withSittingSequence(final Integer sittingSequence) {
            this.sittingSequence = sittingSequence;
            return this;
        }

        public Builder withSittingStart(final String sittingStart) {
            this.sittingStart = sittingStart;
            return this;
        }

        public Builder withValuesFrom(final Sittings sittings) {
            this.channel = sittings.getChannel();
            this.hearing = sittings.getHearing();
            this.judiciary = sittings.getJudiciary();
            this.sittingEnd = sittings.getSittingEnd();
            this.sittingSequence = sittings.getSittingSequence();
            this.sittingStart = sittings.getSittingStart();
            return this;
        }

        public Sittings build() {
            return new Sittings(channel, hearing, judiciary, sittingEnd, sittingSequence, sittingStart);
        }
    }
}
