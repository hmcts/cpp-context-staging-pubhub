package uk.gov.moj.cpp.staging.pubhub.domain;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;
import static uk.gov.justice.staging.pubhub.PublishRequested.publishRequested;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.staging.pubhub.Language;
import uk.gov.justice.staging.pubhub.ListPayload;
import uk.gov.justice.staging.pubhub.PressTransparencyReportGenerated;
import uk.gov.justice.staging.pubhub.PublicReportGenerated;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.StandardList;
import uk.gov.justice.staging.pubhub.json.schema.CourtRoom;
import uk.gov.justice.staging.pubhub.json.schema.PublishLiveStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

public class PubHubAggregate implements Aggregate {

    private static final String STANDARD = "Standard";

    @Override
    public Object apply(final Object event) {
        return match(event).with(
            when(PublishRequested.class).apply(e -> {

            }), when(PressTransparencyReportGenerated.class).apply(e -> {


                }),
                when(PublicReportGenerated.class).apply(e -> {


                }),
            otherwiseDoNothing()
        );
    }

    public Stream<Object> requestPublish(final StandardList standardList) {
        return apply(Stream.of(publishRequested()
                .withListType(STANDARD)
                .withStandardList(standardList)
                .build()));
    }

    public Stream<Object> publishLiveStatus(final String documentName, final ZonedDateTime documentDate, final String version,
                                            final String venueId, final String venueType, final String listType, final String courtCenterName,
                                            final String courtId, final String address1, final String postCode, final String hearingdate,
                                            final List<CourtRoom> courtRooms){
        return apply(Stream.of(PublishLiveStatus.publishLiveStatus()
                .withDocumentName(documentName)
                .withDocumentDate(documentDate)
                .withVersion(version)
                .withVenueId(venueId)
                .withVenueType(venueType)
                .withListType(listType)
                .withCourtId(courtId)
                .withCourtCentreName(courtCenterName)
                .withAddress1(address1)
                .withPostCode(postCode)
                .withHearingDate(hearingdate)
                .withCourtRooms(courtRooms)
        .build()));
    }

    public Stream<Object> requestSjpPressPublish(final String language, final ListPayload payload) {
        return apply(Stream.of(PressTransparencyReportGenerated.pressTransparencyReportGenerated()
                .withListPayload(payload)
                .withLanguage(Language.valueOf(language))
                .build()));
    }

    public Stream<Object> requestSjpPublicPublish(final String language, final ListPayload payload) {
        return apply(Stream.of(PublicReportGenerated.publicReportGenerated()
                .withListPayload(payload)
                .withLanguage(Language.valueOf(language))
                .build()));
    }
}
