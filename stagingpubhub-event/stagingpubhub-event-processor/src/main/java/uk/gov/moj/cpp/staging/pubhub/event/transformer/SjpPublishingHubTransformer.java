package uk.gov.moj.cpp.staging.pubhub.event.transformer;

import static java.util.Objects.nonNull;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.staging.pubhub.schema.Address;
import uk.gov.justice.staging.pubhub.schema.Cases;
import uk.gov.justice.staging.pubhub.schema.CourtHouse;
import uk.gov.justice.staging.pubhub.schema.CourtLists;
import uk.gov.justice.staging.pubhub.schema.CourtRoom;
import uk.gov.justice.staging.pubhub.schema.Document;
import uk.gov.justice.staging.pubhub.schema.Hearing;
import uk.gov.justice.staging.pubhub.schema.IndividualDetails;
import uk.gov.justice.staging.pubhub.schema.Offence;
import uk.gov.justice.staging.pubhub.schema.OrganisationAddress;
import uk.gov.justice.staging.pubhub.schema.OrganisationDetails;
import uk.gov.justice.staging.pubhub.schema.Party;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.justice.staging.pubhub.schema.Session;
import uk.gov.justice.staging.pubhub.schema.Sittings;
import uk.gov.moj.cpp.staging.pubhub.event.service.PartyType;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S3655"})
public class SjpPublishingHubTransformer {
    public static final String PROSECUTOR_NAME = "prosecutorName";
    public static final String LEGAL_ENTITY_NAME = "legalEntityName";
    public static final String DEFENDANT_NAME = "defendantName";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String DATE_OF_BIRTH = "dateOfBirth";
    public static final String AGE = "age";
    public static final String ADDRESS_LINE_1 = "addressLine1";
    public static final String ADDRESS_LINE_2 = "addressLine2";
    public static final String ADDRESS_LINE_3 = "addressLine3";
    public static final String TOWN = "town";
    public static final String COUNTRY = "country";
    public static final String POSTCODE = "postcode";
    public static final String TITLE = "title";
    public static final String OFFENCE_TITLE = "title";
    public static final String OFFENCE_WORDING = "wording";
    public static final String REPORTING_RESTRICTION = "reportingRestriction";
    public static final String CASE_URN = "caseUrn";
    private static final Logger LOGGER = LoggerFactory.getLogger(SjpPublishingHubTransformer.class);
    private static final String VERSION = "1.0";

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

        pubhubMasterBuilder.withCourtLists(buildCourtListsForSjp(envelope, documentType));

        return pubhubMasterBuilder.build();

    }

    private List<CourtLists> buildCourtListsForSjp(final JsonEnvelope envelope, final DocumentType documentType) {
        final List<CourtLists> courtLists = new ArrayList<>();
        final CourtLists courtLists1 = CourtLists.courtLists().withCourtHouse(CourtHouse.courtHouse()
                        .withCourtRoom(buildCourtRoomsForSjp(envelope, documentType)).build())
                .build();
        courtLists.add(courtLists1);
        return courtLists;
    }

    private List<CourtRoom> buildCourtRoomsForSjp(final JsonEnvelope envelope, final DocumentType documentType) {
        final List<CourtRoom> courtRooms = new ArrayList<>();
        final List<Sittings> sittings = new ArrayList<>();

        final List<Session> sessions = new ArrayList<>();
        final List<Hearing> hearings = new ArrayList<>();
        envelope.payloadAsJsonObject().getJsonObject("listPayload").getJsonArray("readyCases")
                .stream().forEach(sc -> hearings.add(buildHearingsForSjp(sc, documentType)));

        final Sittings sitting = Sittings.sittings().withHearing(hearings).build();
        sittings.add(sitting);

        final Session session = Session.session().withSittings(sittings).build();
        sessions.add(session);

        final CourtRoom courtRoom = CourtRoom.courtRoom().withSession(sessions).build();
        courtRooms.add(courtRoom);
        return courtRooms;

    }

    private Hearing buildHearingsForSjp(final JsonValue jsonValue, final DocumentType documentType) {
        final JsonObject jsonObject = (JsonObject) jsonValue;
        final String caseUrn = nonNull(jsonObject.get(CASE_URN)) ? jsonObject.getString(CASE_URN) : "";

        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withParty(buildDefendantsForSjp(jsonObject))
                .withOffence(buildOffencesForSjp(jsonObject, documentType));

        if (!caseUrn.isEmpty()) {
            final List<Cases> cases = new ArrayList<>();
            final Cases case1 = Cases.cases().withCaseUrn(caseUrn).build();
            cases.add(case1);
            hearingBuilder.withCases(cases);
        }

        return hearingBuilder.build();
    }

    private List<Party> buildDefendantsForSjp(final JsonObject jsonObject) {

        final List<Party> parties = new ArrayList<>();

        if (JsonObjects.getString(jsonObject, PROSECUTOR_NAME).isPresent()) {
            final Party prosecutor = buildProsecutorDetails(jsonObject);
            parties.add(prosecutor);
        }

        if (JsonObjects.getString(jsonObject, LEGAL_ENTITY_NAME).isPresent()) {
            final Party legalEntity = buildOrganisationDetails(jsonObject);
            parties.add(legalEntity);
        }

        final String defendantName = nonNull(jsonObject.get(DEFENDANT_NAME)) ? jsonObject.getString(DEFENDANT_NAME) : "";
        if (!defendantName.isEmpty()) {
            final Party defendant = buildIndividualDetails(jsonObject);
            parties.add(defendant);
        }

        return parties;
    }

    private Party buildProsecutorDetails(final JsonObject jsonObject) {
        final OrganisationDetails.Builder organisationDetailsBuilder = OrganisationDetails.organisationDetails();
        JsonObjects.getString(jsonObject, PROSECUTOR_NAME).ifPresent(organisationDetailsBuilder::withOrganisationName);

        return Party.party()
                .withPartyRole(PartyType.PROSECUTOR.toString())
                .withOrganisationDetails(organisationDetailsBuilder.build()).build();
    }

    private Party buildIndividualDetails(final JsonObject jsonObject) {
        final IndividualDetails.Builder individualBuilder = IndividualDetails.individualDetails();
        JsonObjects.getString(jsonObject, TITLE).ifPresent(individualBuilder::withTitle);
        JsonObjects.getString(jsonObject, FIRST_NAME).ifPresent(individualBuilder::withIndividualForenames);
        JsonObjects.getString(jsonObject, LAST_NAME).ifPresent(individualBuilder::withIndividualSurname);
        JsonObjects.getString(jsonObject, DATE_OF_BIRTH).ifPresent(individualBuilder::withDateOfBirth);
        JsonObjects.getString(jsonObject, AGE).ifPresent(age -> individualBuilder.withAge(Integer.parseInt(age)));

        final List<String> addressLines = new ArrayList<>();
        JsonObjects.getString(jsonObject, ADDRESS_LINE_1).ifPresent(addressLines::add);
        JsonObjects.getString(jsonObject, ADDRESS_LINE_2).ifPresent(addressLines::add);
        JsonObjects.getString(jsonObject, ADDRESS_LINE_3).ifPresent(addressLines::add);

        final Address.Builder addressBuilder = Address.address().withLine(addressLines);
        if (!addressLines.isEmpty()) {
            addressBuilder.withLine(addressLines);
        }

        JsonObjects.getString(jsonObject, TOWN).ifPresent(addressBuilder::withTown);
        JsonObjects.getString(jsonObject, COUNTRY).ifPresent(addressBuilder::withCounty);
        JsonObjects.getString(jsonObject, POSTCODE).ifPresent(addressBuilder::withPostCode);
        individualBuilder.withAddress(addressBuilder.build());

        return Party.party()
                .withPartyRole(PartyType.ACCUSED.toString())
                .withIndividualDetails(individualBuilder.build())
                .build();

    }

    private Party buildOrganisationDetails(final JsonObject jsonObject) {
        final OrganisationDetails.Builder organisationDetailsBuilder = OrganisationDetails.organisationDetails();
        JsonObjects.getString(jsonObject, LEGAL_ENTITY_NAME).ifPresent(organisationDetailsBuilder::withOrganisationName);

        final List<String> addressLines = new ArrayList<>();
        JsonObjects.getString(jsonObject, ADDRESS_LINE_1).ifPresent(addressLines::add);
        JsonObjects.getString(jsonObject, ADDRESS_LINE_2).ifPresent(addressLines::add);
        JsonObjects.getString(jsonObject, ADDRESS_LINE_3).ifPresent(addressLines::add);

        final OrganisationAddress.Builder addressBuilder = OrganisationAddress.organisationAddress();
        if (!addressLines.isEmpty()) {
            addressBuilder.withLine(addressLines);
        }

        JsonObjects.getString(jsonObject, TOWN).ifPresent(addressBuilder::withTown);
        JsonObjects.getString(jsonObject, COUNTRY).ifPresent(addressBuilder::withCounty);
        JsonObjects.getString(jsonObject, POSTCODE).ifPresent(addressBuilder::withPostCode);
        organisationDetailsBuilder.withOrganisationAddress(addressBuilder.build());

        return Party.party()
                .withPartyRole(PartyType.ACCUSED.toString())
                .withOrganisationDetails(organisationDetailsBuilder.build()).build();

    }

    private List<Offence> buildOffencesForSjp(final JsonObject jsonObject, final DocumentType documentType) {
        final List<Offence> offences = new ArrayList<>();

        jsonObject.getJsonArray("sjpOffences").stream().forEach(jsonOffenceValue -> {
            final Offence.Builder offenceBuilder = Offence.offence();
            final JsonObject jsonOffenceObject = (JsonObject) jsonOffenceValue;
            JsonObjects.getString(jsonOffenceObject, OFFENCE_TITLE).ifPresent(offenceBuilder::withOffenceTitle);
            JsonObjects.getString(jsonOffenceObject, OFFENCE_WORDING).ifPresent(offenceBuilder::withOffenceWording);
            if (documentType.equals(DocumentType.SJP_PRESS_LIST) || documentType.equals(DocumentType.SJP_DELTA_PRESS_LIST)) {
                offenceBuilder.withReportingRestriction(nonNull(jsonOffenceObject.get(REPORTING_RESTRICTION)) && jsonOffenceObject.getBoolean(REPORTING_RESTRICTION));
            }
            offences.add(offenceBuilder.build());
        });

        return offences;
    }
}
