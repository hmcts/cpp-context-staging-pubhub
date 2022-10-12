package uk.gov.moj.cpp.staging.pubhub.event.transformer;

import static java.util.Objects.nonNull;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.MAGS_STANDARD_LIST_ENGLISH;

import uk.gov.justice.staging.pubhub.Defendants;
import uk.gov.justice.staging.pubhub.HearingDates;
import uk.gov.justice.staging.pubhub.Offences;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.StandardList;
import uk.gov.justice.staging.pubhub.schema.Address;
import uk.gov.justice.staging.pubhub.schema.Cases;
import uk.gov.justice.staging.pubhub.schema.CourtHouse;
import uk.gov.justice.staging.pubhub.schema.CourtLists;
import uk.gov.justice.staging.pubhub.schema.CourtRoom;
import uk.gov.justice.staging.pubhub.schema.Document;
import uk.gov.justice.staging.pubhub.schema.Hearing;
import uk.gov.justice.staging.pubhub.schema.IndividualDetails;
import uk.gov.justice.staging.pubhub.schema.Judiciary;
import uk.gov.justice.staging.pubhub.schema.Offence;
import uk.gov.justice.staging.pubhub.schema.OrganisationDetails;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S3776", "squid:S1125", "squid:S1602", "squid:S3655", "squid:S1188", "pmd:NullAssignment"})
public class PublishingHubTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingHubTransformer.class);
    private static final String VERSION = "1.0";
    private static final String MAGS_COURT = "Magistrates Court";
    private static final String ACCUSED = "ACCUSED";

    public PubhubMaster transformStandardList(final PublishRequested publishRequested) {
        LOGGER.info("Transforming standard court list {}", publishRequested);
        final PubhubMaster.Builder pubhubMasterBuilder = PubhubMaster.pubhubMaster();

        final StandardList standardList = publishRequested.getStandardList();
        final List<HearingDates> hearingDates = standardList.getHearingDates();

        if (nonNull(hearingDates)) {
            final Document document = Document.document()
                    .withDocumentName(MAGS_STANDARD_LIST_ENGLISH.getValue())
                    .withPublicationDate(ZonedDateTime.now().toString())
                    .withVersion(VERSION)
                    .build();
            pubhubMasterBuilder.withDocument(document);

            final Venue venue = Venue.venue()
                    .withVenueName(standardList.getCourtCentreName())
                    .withVenueAddress(VenueAddress.venueAddress()
                            .withLine(Arrays.asList(standardList.getCourtCentreAddress1()))
                            .build())
                    .withVenueType(MAGS_COURT)
                    .withVenueCode(VenueCode.venueCode()
                            .withVenueId(standardList.getOuCode())
                            .build())
                    .build();
            pubhubMasterBuilder.withVenue(venue);
            pubhubMasterBuilder.withCourtLists(buildCourtLists(hearingDates));
        }

        return pubhubMasterBuilder.build();
    }

    private List<CourtLists> buildCourtLists(final List<HearingDates> hearingDates) {
        final List<CourtLists> courtLists = new ArrayList<>();

        final CourtLists courtLists1 = CourtLists.courtLists()
                .withCourtHouse(CourtHouse.courtHouse()
                        .withCourtRoom(buildCourtRooms(hearingDates))
                        .build())
                .build();

        courtLists.add(courtLists1);
        return courtLists;
    }

    private List<CourtRoom> buildCourtRooms(final List<HearingDates> hearingDates) {
        final List<CourtRoom> courtRooms = new ArrayList<>();

        hearingDates.forEach(hearingDates1 ->
                hearingDates1.getCourtRooms().forEach(courtRoom1 -> {
                    final List<Session> sessions = new ArrayList<>();

                    courtRoom1.getTimeslots().forEach(timeslot1 -> {
                        final List<Sittings> sittings = new ArrayList<>();
                        final List<Hearing> hearings = new ArrayList<>();

                        timeslot1.getHearings().forEach(hearing1 -> {
                            hearings.add(buildHearings(hearing1));
                        });

                        final Sittings sitting = Sittings.sittings()
                                .withHearing(hearings)
                                .withJudiciary(buildJudiciaries(courtRoom1.getJudiciaryNames()))
                                .build();
                        sittings.add(sitting);

                        final Session session = Session.session()
                                .withSessionStartTime(hearingDates1.getHearingDate())
                                .withSittings(sittings).build();
                        sessions.add(session);
                    });

                    final CourtRoom courtRoom = CourtRoom.courtRoom()
                            .withCourtRoomName(courtRoom1.getCourtRoomName())
                            .withSession(sessions)
                            .build();
                    courtRooms.add(courtRoom);
                }));

        return courtRooms;
    }

    private List<Judiciary> buildJudiciaries(final String judiciaryNames) {
        final List<Judiciary> judiciaries = new ArrayList<>();

        if (nonNull(judiciaryNames) && !judiciaryNames.isEmpty()) {
            final String[] names = judiciaryNames.split(",");
            for (final String name : names) {
                final String[] splitName = name.trim().split(" ");
                final int length = splitName.length;
                if (length > 0) {
                    final Judiciary judiciary = Judiciary.judiciary()
                            .withJohTitle(splitName[0])
                            .withJohNameSurname(length > 2 ? splitName[1] + splitName[2] : splitName[1])
                            .build();
                    judiciaries.add(judiciary);
                }
            }
        }
        return judiciaries;
    }

    private Hearing buildHearings(final uk.gov.justice.staging.pubhub.Hearing hearing1) {
        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withHearingSequence(hearing1.getSequence())
                .withHearingType(hearing1.getHearingType())
                .withParty(buildDefendants(hearing1))
                .withOffence(buildOffences(hearing1.getDefendants()));

        final List<Cases> cases = new ArrayList<>();
        final Cases case1 = Cases.cases()
                .withCaseUrn(hearing1.getCaseNumber())
                .build();
        cases.add(case1);
        hearingBuilder.withCases(cases);

        return hearingBuilder.build();
    }

    private List<Party> buildDefendants(final uk.gov.justice.staging.pubhub.Hearing hearing1) {
        final List<Party> parties = new ArrayList<>();

        hearing1.getDefendants().forEach(defendants1 -> {
            final Party.Builder party = Party.party()
                    .withPartyRole(ACCUSED)
                    .withOrganisationDetails(OrganisationDetails.organisationDetails()
                            .withOrganisationName(defendants1.getOrganisationName())
                            .build());

            final IndividualDetails.Builder builder = IndividualDetails.individualDetails()
                    .withIndividualForenames(defendants1.getFirstName())
                    .withIndividualSurname(defendants1.getSurname())
                    .withDateOfBirth(defendants1.getDateOfBirth());

            final uk.gov.justice.core.courts.Address address = defendants1.getAddress();
            builder.withAddress(Address.address()
                    .withLine(Arrays.asList(address.getAddress1()))
                    .withTown(address.getAddress2())
                    .withCounty(address.getAddress3())
                    .withPostCode(address.getAddress4())
                    .build());


            party.withIndividualDetails(builder.build());
            parties.add(party.build());
        });
        return parties;
    }

    private Offence buildOffences(final List<Defendants> defendants) {
        final Offence.Builder offence = Offence.offence();
        final Defendants defendants1 = defendants.get(0);
        final List<Offences> offences = defendants1.getOffences();

        if (nonNull(offences)) {
            offence.withOffenceTitle(offences.get(0).getOffenceTitle())
                    .withOffenceWording(offences.get(0).getOffenceWording())
                    .withReportingRestriction(nonNull(defendants1.getReportingRestrictions()) && !defendants1.getReportingRestrictions().isEmpty() ? true : false)
                    .build();
        }

        return offence.build();
    }
}
