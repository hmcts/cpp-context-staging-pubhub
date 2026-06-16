package uk.gov.moj.cpp.staging.pubhub.event.transformer;

import static java.util.Objects.nonNull;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.CROWN_LCSU;

import uk.gov.justice.staging.pubhub.json.schema.CourtRoom;
import uk.gov.justice.staging.pubhub.json.schema.Defendant;
import uk.gov.justice.staging.pubhub.json.schema.HearingEvent;
import uk.gov.justice.staging.pubhub.json.schema.PublishLiveStatus;
import uk.gov.justice.staging.pubhub.schema.Address;
import uk.gov.justice.staging.pubhub.schema.Cases;
import uk.gov.justice.staging.pubhub.schema.CourtHouse;
import uk.gov.justice.staging.pubhub.schema.CourtLists;
import uk.gov.justice.staging.pubhub.schema.Document;
import uk.gov.justice.staging.pubhub.schema.Hearing;
import uk.gov.justice.staging.pubhub.schema.IndividualDetails;
import uk.gov.justice.staging.pubhub.schema.Lcsu;
import uk.gov.justice.staging.pubhub.schema.Party;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.justice.staging.pubhub.schema.Session;
import uk.gov.justice.staging.pubhub.schema.Sittings;
import uk.gov.justice.staging.pubhub.schema.Venue;
import uk.gov.justice.staging.pubhub.schema.VenueAddress;
import uk.gov.justice.staging.pubhub.schema.VenueCode;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188"})
public class LcsPublishingHubTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LcsPublishingHubTransformer.class);

    public PubhubMaster transformLcsu(final PublishLiveStatus publishLiveStatus) {
        LOGGER.info("Transforming standard court list {}", publishLiveStatus);
        final PubhubMaster.Builder pubhubMasterBuilder = PubhubMaster.pubhubMaster();

        final Document document = Document.document()
                .withDocumentName(CROWN_LCSU.getValue())
                .withPublicationDate(ZonedDateTime.now().toString())
                .withVersion(publishLiveStatus.getVersion())
                .build();
        pubhubMasterBuilder.withDocument(document);

        final Venue venue = Venue.venue()
                .withVenueName(publishLiveStatus.getCourtCentreName())
                .withVenueAddress(VenueAddress.venueAddress()
                        .withLine(Arrays.asList(publishLiveStatus.getAddress1()))
                        .build())
                .withVenueType(publishLiveStatus.getVenueType())
                .withVenueCode(VenueCode.venueCode()
                        .withVenueId(publishLiveStatus.getVenueId())
                        .build())
                .build();
        pubhubMasterBuilder.withVenue(venue);
        pubhubMasterBuilder.withCourtLists(buildCourtLists(publishLiveStatus.getCourtRooms()));

        return pubhubMasterBuilder.build();
    }

    private List<CourtLists> buildCourtLists(final List<CourtRoom> courtRooms) {
        final List<CourtLists> courtLists = new ArrayList<>();

        final List<uk.gov.justice.staging.pubhub.schema.CourtRoom> courtRooms1 = new ArrayList<>();
        courtRooms.forEach(courtRoom -> {
            final uk.gov.justice.staging.pubhub.schema.CourtRoom courtRoom1 = uk.gov.justice.staging.pubhub.schema.CourtRoom.courtRoom()
//                    .withCourtRoomId(nonNull(courtRoom.getRoomId())? Integer.parseInt(courtRoom.getRoomId()): null) //Commenting this code since courtRoomId is a UUID and P&I is expecting an int
                    .withCourtRoomName(courtRoom.getCourtRoomName())
                    .withSession(buildSessions(courtRoom))
                    .build();

            courtRooms1.add(courtRoom1);
        });

        final CourtLists courtLists1 = CourtLists.courtLists()
                .withCourtHouse(CourtHouse.courtHouse()
                        .withCourtRoom(courtRooms1)
                        .build())
                .build();

        courtLists.add(courtLists1);
        return courtLists;
    }

    private List<Session> buildSessions(final CourtRoom courtRoom) {
        final List<Session> sessions = new ArrayList<>();

        courtRoom.getSessions().forEach(session -> {
            final List<Sittings> sittings = new ArrayList<>();

            session.getSittings().forEach(sitting -> {
                final List<Hearing> hearings = new ArrayList<>();

                sitting.getHearing().forEach(hearing -> {
                    final List<Cases> cases = new ArrayList<>();
                    final List<HearingEvent> hearingEvents = hearing.getHearingEvents();

                    final List<String> caseNumber = hearing.getCaseNumber();
                    final Cases case1 = Cases.cases()
                            .withCaseUrn(caseNumber.get(0))
                            .build();
                    cases.add(case1);

                    if (nonNull(hearingEvents)) {
                        final Lcsu lcsu = Lcsu.lcsu()
                                .withLcsuDateTime(hearingEvents.get(0).getHearingEventTime().toString())
                                .withLcsuDescription(hearingEvents.get(0).getHearingEvent())
                                .build();

                        final Hearing hearing1 = Hearing.hearing()
                                .withCases(cases)
                                .withLcsu(lcsu)
                                .withParty(buildDefendants(hearing.getDefendants()))
                                .build();

                        hearings.add(hearing1);
                    }
                });
                final Sittings sitting1 = Sittings.sittings()
                        .withHearing(hearings)
                        .build();
                sittings.add(sitting1);
            });
            final Session session1 = Session.session()
                    .withSittings(sittings)
                    .build();
            sessions.add(session1);
        });
        return sessions;
    }

    private List<Party> buildDefendants(final List<Defendant> defendants) {
        final List<Party> parties = new ArrayList<>();
        defendants.forEach(defendant -> {
            final Party party = Party.party()
                    .withIndividualDetails(IndividualDetails.individualDetails()
                            .withIndividualForenames(defendant.getFirstName())
                            .withIndividualSurname(defendant.getLastName())
                            .withAddress(buildAddress(defendant))
                            .withDateOfBirth(defendant.getDateOfBirth())
                            .withAsns(Arrays.asList(defendant.getArrestSummonsNumber()))
                            .withNationality(nonNull(defendant.getNationality()) ? String.valueOf(defendant.getNationality()) : StringUtils.EMPTY)
                            .build())
                    .build();
            // Offences mapping TBD
            parties.add(party);
        });
        return parties;
    }

    private Address buildAddress(final Defendant defendant) {
        if (nonNull(defendant.getAddress())) {
            return Address.address()
                    .withPostCode(defendant.getAddress().getPostcode())
                    .withLine(buildAddressLine(defendant.getAddress()))
                    .withTown(defendant.getAddress().getAddress2())
                    .withCounty(defendant.getAddress().getAddress3())
                    .build();
        }
        return Address.address().build();
    }

    private List<String> buildAddressLine(final uk.gov.justice.core.courts.Address address) {
        final List<String> addressList = new ArrayList<>();
        addressList.add(address.getAddress1());
        return addressList;
    }
}