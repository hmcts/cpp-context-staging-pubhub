package uk.gov.moj.cpp.stagingpubhub.domain;

import uk.gov.justice.staging.pubhub.schema.Judiciary;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"squid:S1067", "squid:S2384"})
public class Session implements Serializable {
    private static final long serialVersionUID = -5384779540784619225L;

    private final List<Judiciary> judiciary;

    private final List<String> sessionChannel;

    private final String sessionId;

    private final Integer sessionSequenceNumber;

    private final String sessionStartTime;

    private final String sessionType;

    private final List<Sittings> sittings;

    public Session(final List<Judiciary> judiciary, final List<String> sessionChannel, final String sessionId, final Integer sessionSequenceNumber, final String sessionStartTime, final String sessionType, final List<Sittings> sittings) {
        this.judiciary = judiciary;
        this.sessionChannel = sessionChannel;
        this.sessionId = sessionId;
        this.sessionSequenceNumber = sessionSequenceNumber;
        this.sessionStartTime = sessionStartTime;
        this.sessionType = sessionType;
        this.sittings = sittings;
    }

    public static Builder session() {
        return new Session.Builder();
    }

    public List<Judiciary> getJudiciary() {
        return judiciary;
    }

    public List<String> getSessionChannel() {
        return sessionChannel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Integer getSessionSequenceNumber() {
        return sessionSequenceNumber;
    }

    public String getSessionStartTime() {
        return sessionStartTime;
    }

    public String getSessionType() {
        return sessionType;
    }

    public List<Sittings> getSittings() {
        return sittings;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Session that = (Session) obj;

        return java.util.Objects.equals(this.judiciary, that.judiciary) &&
                java.util.Objects.equals(this.sessionChannel, that.sessionChannel) &&
                java.util.Objects.equals(this.sessionId, that.sessionId) &&
                java.util.Objects.equals(this.sessionSequenceNumber, that.sessionSequenceNumber) &&
                java.util.Objects.equals(this.sessionStartTime, that.sessionStartTime) &&
                java.util.Objects.equals(this.sessionType, that.sessionType) &&
                java.util.Objects.equals(this.sittings, that.sittings);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(judiciary, sessionChannel, sessionId, sessionSequenceNumber, sessionStartTime, sessionType, sittings);
    }

    @Override
    public String toString() {
        return "Session{" +
                "judiciary='" + judiciary + "'," +
                "sessionChannel='" + sessionChannel + "'," +
                "sessionId='" + sessionId + "'," +
                "sessionSequenceNumber='" + sessionSequenceNumber + "'," +
                "sessionStartTime='" + sessionStartTime + "'," +
                "sessionType='" + sessionType + "'," +
                "sittings='" + sittings + "'" +
                "}";
    }

    public static class Builder {
        private List<Judiciary> judiciary;

        private List<String> sessionChannel;

        private String sessionId;

        private Integer sessionSequenceNumber;

        private String sessionStartTime;

        private String sessionType;

        private List<Sittings> sittings;

        public Builder withJudiciary(final List<Judiciary> judiciary) {
            this.judiciary = judiciary;
            return this;
        }

        public Builder withSessionChannel(final List<String> sessionChannel) {
            this.sessionChannel = sessionChannel;
            return this;
        }

        public Builder withSessionId(final String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder withSessionSequenceNumber(final Integer sessionSequenceNumber) {
            this.sessionSequenceNumber = sessionSequenceNumber;
            return this;
        }

        public Builder withSessionStartTime(final String sessionStartTime) {
            this.sessionStartTime = sessionStartTime;
            return this;
        }

        public Builder withSessionType(final String sessionType) {
            this.sessionType = sessionType;
            return this;
        }

        public Builder withSittings(final List<Sittings> sittings) {
            this.sittings = sittings;
            return this;
        }

        public Builder withValuesFrom(final Session session) {
            this.judiciary = session.getJudiciary();
            this.sessionChannel = session.getSessionChannel();
            this.sessionId = session.getSessionId();
            this.sessionSequenceNumber = session.getSessionSequenceNumber();
            this.sessionStartTime = session.getSessionStartTime();
            this.sessionType = session.getSessionType();
            this.sittings = session.getSittings();
            return this;
        }

        public Session build() {
            return new Session(judiciary, sessionChannel, sessionId, sessionSequenceNumber, sessionStartTime, sessionType, sittings);
        }
    }
}
