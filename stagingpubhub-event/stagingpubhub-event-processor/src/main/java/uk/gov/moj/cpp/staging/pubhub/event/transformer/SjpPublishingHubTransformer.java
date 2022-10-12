package uk.gov.moj.cpp.staging.pubhub.event.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import uk.gov.justice.staging.pubhub.schema.Address;
import uk.gov.justice.staging.pubhub.schema.Cases;
import uk.gov.justice.staging.pubhub.schema.CourtHouse;
import uk.gov.justice.staging.pubhub.schema.CourtLists;
import uk.gov.justice.staging.pubhub.schema.CourtRoom;
import uk.gov.justice.staging.pubhub.schema.Document;
import uk.gov.justice.staging.pubhub.schema.Hearing;
import uk.gov.justice.staging.pubhub.schema.IndividualDetails;
import uk.gov.justice.staging.pubhub.schema.Offence;
import uk.gov.justice.staging.pubhub.schema.OrganisationDetails;
import uk.gov.justice.staging.pubhub.schema.Party;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.justice.staging.pubhub.schema.Session;
import uk.gov.justice.staging.pubhub.schema.Sittings;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@SuppressWarnings({"squid:S3776", "squid:S1125", "squid:S1602", "squid:S3655", "squid:S1188", "pmd:NullAssignment", "squid:MethodCyclomaticComplexity"})
public class SjpPublishingHubTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SjpPublishingHubTransformer.class);
    private static final String VERSION = "1.0";
    public static final String PROSECUTOR_NAME = "prosecutorName";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String DEFENDANT_DATE_OF_BIRTH = "defendantDateOfBirth";
    public static final String ADDRESS_LINE_1 = "addressLine1";
    public static final String ADDRESS_LINE_2 = "addressLine2";
    public static final String TOWN = "town";
    public static final String COUNTY = "county";
    public static final String POSTCODE = "postcode";
    public static final String TITLE = "title";
    public static final String OFFENCE_WORDING = "offenceWording";
    public static final String PRESS_RESTRICTION_REQUESTED = "pressRestrictionRequested";
    public static final String CASE_URN = "caseUrn";


    public PubhubMaster transformSjpList(final JsonEnvelope envelope, final DocumentType documentType) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Transforming sjp public/press court list {}", envelope.toObfuscatedDebugString());
        }

        final PubhubMaster.Builder pubhubMasterBuilder = PubhubMaster.pubhubMaster();

        final Document document = Document.document()
                .withDocumentName(documentType.getValue())
                .withPublicationDate(envelope.payloadAsJsonObject().getJsonObject("listPayload").getString("generatedDateAndTime"))
                .withVersion(VERSION)
                .build();
        pubhubMasterBuilder.withDocument(document);

        pubhubMasterBuilder.withCourtLists(buildCourtListsForSjp(envelope));

        return pubhubMasterBuilder.build();

    }

    private List<CourtLists> buildCourtListsForSjp(final JsonEnvelope envelope) {
        final List<CourtLists> courtLists = new ArrayList<>();
        final CourtLists courtLists1 = CourtLists.courtLists()
                .withCourtHouse(CourtHouse.courtHouse()
                        .withCourtRoom(buildCourtRoomsForSjp(envelope))
                        .build())
                .build();
        courtLists.add(courtLists1);
        return courtLists;
    }

    private List<CourtRoom> buildCourtRoomsForSjp(final JsonEnvelope envelope) {
        final List<CourtRoom> courtRooms = new ArrayList<>();
        final List<Sittings> sittings = new ArrayList<>();

        final List<Session> sessions = new ArrayList<>();
        final List<Hearing> hearings = new ArrayList<>();
        envelope.payloadAsJsonObject().getJsonObject("listPayload").getJsonArray("readyCases").stream().forEach(
                sc -> {
                hearings.add(buildHearingsForSjp(sc));
                });
        final Sittings sitting = Sittings.sittings()
                .withHearing(hearings)
                .build();
        sittings.add(sitting);

        final Session session = Session.session()

                .withSittings(sittings).build();
        sessions.add(session);

        final CourtRoom courtRoom = CourtRoom.courtRoom()
                .withSession(sessions)
                .build();
        courtRooms.add(courtRoom);
        return courtRooms;

    }

    private Hearing buildHearingsForSjp(final JsonValue jsonValue) {
        final JsonObject jsonObject = (JsonObject)jsonValue;
        final String caseUrn = nonNull(jsonObject.get(CASE_URN)) ? jsonObject.getString(CASE_URN) : "";

        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withParty(buildDefendantsForSjp(jsonObject))
                .withOffence(buildOffencesForSjp(jsonObject));

        if (!caseUrn.isEmpty()) {
            final List<Cases> cases = new ArrayList<>();
            final Cases case1 = Cases.cases()
                    .withCaseUrn(caseUrn)
                    .build();
            cases.add(case1);
            hearingBuilder.withCases(cases);
        }

        return hearingBuilder.build();
    }

    private List<Party> buildDefendantsForSjp(final JsonObject jsonObject) {

        final List<Party> parties = new ArrayList<>();

         if(nonNull(jsonObject.getString(PROSECUTOR_NAME))) {
            final Party.Builder prosecutorParty = Party.party()
                    .withPartyRole(nonNull(jsonObject.getString(PROSECUTOR_NAME))?"PROSECUTOR":"DEFENDANT")
                    .withOrganisationDetails(OrganisationDetails.organisationDetails()
                            .withOrganisationName(nonNull(jsonObject.get(PROSECUTOR_NAME)) ? jsonObject.getString(PROSECUTOR_NAME)  : null)
                            .build());
            parties.add(prosecutorParty.build());
        }

            final Party.Builder party = Party.party();

            final IndividualDetails.Builder builder = IndividualDetails.individualDetails()
                    .withIndividualForenames(nonNull(jsonObject.get(FIRST_NAME)) ? jsonObject.getString(FIRST_NAME)  : null)
                    .withIndividualSurname(nonNull(jsonObject.get(LAST_NAME)) ? jsonObject.getString(LAST_NAME)  : null)
                    .withDateOfBirth(nonNull(jsonObject.get(DEFENDANT_DATE_OF_BIRTH)) ? jsonObject.getString(DEFENDANT_DATE_OF_BIRTH)  : null);


             final List<String> addressLines = new ArrayList<>();
             if(nonNull(jsonObject.get(ADDRESS_LINE_1))){
                 addressLines.add(jsonObject.getString(ADDRESS_LINE_1));
             }
             if(nonNull(jsonObject.get(ADDRESS_LINE_2))){
                addressLines.add(jsonObject.getString(ADDRESS_LINE_2));
             }
            builder.withAddress(Address.address()
                    .withLine(addressLines)
                    .withTown(nonNull(jsonObject.get(TOWN)) ? jsonObject.getString(TOWN)  : null)
                    .withCounty(nonNull(jsonObject.get(COUNTY)) ? jsonObject.getString(COUNTY)  : null)
                    .withPostCode(nonNull(jsonObject.get(POSTCODE)) ? jsonObject.getString(POSTCODE)  : null)
                    .build());


            party.withIndividualDetails(builder.build());
            parties.add(party.build());

        return parties;
    }
    private Offence buildOffencesForSjp(final JsonObject jsonObject) {
        final Offence.Builder offence = Offence.offence();
        final JsonObject jsonOffenceObject = jsonObject.getJsonArray("sjpOffences").getJsonObject(0);
        if(nonNull(jsonOffenceObject)){
            offence.withOffenceTitle(nonNull(jsonOffenceObject.get(TITLE)) ? jsonOffenceObject.getString(TITLE)  : null)
                    .withOffenceWording(nonNull(jsonOffenceObject.get(OFFENCE_WORDING)) ? jsonOffenceObject.getString(OFFENCE_WORDING)  : null)
                    .withReportingRestriction(nonNull(jsonOffenceObject.get(PRESS_RESTRICTION_REQUESTED)) && "true".equals(jsonOffenceObject.getString(PRESS_RESTRICTION_REQUESTED))?true:false)
                    .build();
        }
        return offence.build();
    }
}
