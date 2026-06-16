package uk.gov.moj.cpp.staging.pubhub.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelopeProvider;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.justice.staging.pubhub.PressTransparencyReportGenerated;
import uk.gov.justice.staging.pubhub.PublicReportGenerated;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.RequestType;
import uk.gov.justice.staging.pubhub.json.schema.PublishLiveStatus;
import uk.gov.moj.cpp.staging.pubhub.command.handler.util.FileUtil;
import uk.gov.moj.cpp.staging.pubhub.domain.PubHubAggregate;

import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StagingPubHubCommandHandlerTest {

    private static final String STANDARD = "Standard";

    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = EnveloperFactory.createEnveloperWithEvents(
            PublishRequested.class,
            PublishLiveStatus.class,
            PublicReportGenerated.class,
            PressTransparencyReportGenerated.class);

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @InjectMocks
    private StagingPubHubCommandHandler stagingPubHubCommandHandler;

    @Test
    public void shouldHandlerReceiveAllegationsCommand() {
        assertThat(new StagingPubHubCommandHandler(), isHandler(COMMAND_HANDLER)
                .with(method("handlePublishStandard")
                        .thatHandles("stagingpubhub.command.handler.publish-standard-list")
                ));
    }

    @Test
    public void handlePublishStandard() throws EventStreamException {
        final PubHubAggregate pubHubAggregate = new PubHubAggregate();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, PubHubAggregate.class)).thenReturn(pubHubAggregate);

        final JsonObject payload = FileUtil.givenPayload("stub-data/stagingpubhub.command.handler.publish-standard-list.json");
        final JsonEnvelope commandEnvelope = createCommandEnvelope(payload, "stagingpubhub.command.handler.publish-standard-list");
        stagingPubHubCommandHandler.handlePublishStandard(commandEnvelope);

        verifySubscriberHandlerResults("stagingpubhub.event.publish-requested", "$.listType", STANDARD);
    }

    @Test
    public void handlePublishStandardWithMultipleHearing() throws EventStreamException {
        final PubHubAggregate pubHubAggregate = new PubHubAggregate();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, PubHubAggregate.class)).thenReturn(pubHubAggregate);

        final JsonObject payload = FileUtil.givenPayload("stub-data/publish-standard-list.json");
        final JsonEnvelope commandEnvelope = createCommandEnvelope(payload, "stagingpubhub.command.handler.publish-standard-list");
        stagingPubHubCommandHandler.handlePublishStandard(commandEnvelope);

        verifySubscriberHandlerResults("stagingpubhub.event.publish-requested", "$.listType", STANDARD);
    }

    @Test
    public void handleLiveStatusPublished() throws EventStreamException {
        final PubHubAggregate pubHubAggregate = new PubHubAggregate();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, PubHubAggregate.class)).thenReturn(pubHubAggregate);

        final JsonObject payload = FileUtil.givenPayload("stub-data/stagingpubhub.command.handler.live-status-published.json");
        final JsonEnvelope commandEnvelope = createCommandEnvelope(payload, "stagingpubhub.command.handler.live-status-published");
        stagingPubHubCommandHandler.handleLiveStatusPublished(commandEnvelope);

        verifySubscriberHandlerResults("stagingpubhub.event.publish-live-status", "$.documentName", "Live Case Status Update");
    }

    @Test
    public void handleLiveStatusPublishedWithSingleHearingCase() throws EventStreamException {
        final PubHubAggregate pubHubAggregate = new PubHubAggregate();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, PubHubAggregate.class)).thenReturn(pubHubAggregate);

        final JsonObject payload = FileUtil.givenPayload("stub-data/live-status-published.json");
        final JsonEnvelope commandEnvelope = createCommandEnvelope(payload, "stagingpubhub.command.handler.live-status-published");
        stagingPubHubCommandHandler.handleLiveStatusPublished(commandEnvelope);

        verifySubscriberHandlerResults("stagingpubhub.event.publish-live-status", "$.documentName", "Live Case Status Update");
    }

    @Test
    public void handleSjpPressListPublished() throws EventStreamException {
        final PubHubAggregate pubHubAggregate = new PubHubAggregate();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, PubHubAggregate.class)).thenReturn(pubHubAggregate);

        final JsonObject payload = FileUtil.givenPayload("stub-data/stagingpubhub.command.handler.sjp-press-published.json");
        final JsonEnvelope commandEnvelope = createCommandEnvelope(payload, "stagingpubhub.command.handler.sjp-press-published");
        stagingPubHubCommandHandler.handleSjpPressReportPublished(commandEnvelope);

        verifySubscriberHandlerResults("stagingpubhub.event.sjp-press-published", "$.requestType", RequestType.FULL.toString());
    }

    @Test
    public void handleSjpPublicListPublished() throws EventStreamException {
        final PubHubAggregate pubHubAggregate = new PubHubAggregate();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, PubHubAggregate.class)).thenReturn(pubHubAggregate);

        final JsonObject payload = FileUtil.givenPayload("stub-data/stagingpubhub.command.handler.sjp-public-published.json");
        final JsonEnvelope commandEnvelope = createCommandEnvelope(payload, "stagingpubhub.command.handler.sjp-public-published");
        stagingPubHubCommandHandler.handleSjpPublicReportPublished(commandEnvelope);

        verifySubscriberHandlerResults("stagingpubhub.event.sjp-public-published", "$.requestType", RequestType.FULL.toString());
    }

    private JsonObject buildStandardListPayload() {
        return createObjectBuilder()
                .add("standardList",
                        createObjectBuilder()
                                .add("listType", STANDARD)
                                .add("courtCentreName", "Lavender Hill Magistrates' Court")
                                .add("courtCentreAddress1", "176A Lavender Hill London")
                                .add("courtCentreAddress2", "   SW11 1JU")
                                .add("welshCourtCentreName", "Llys Ynadon Lavender Hill")
                                .add("welshCourtCentreAddress1", " ")
                                .add("welshCourtCentreAddress2", "   SW11 1JU")
                                .add("hearingDates", createArrayBuilder().add(createObjectBuilder()
                                        .add("hearingDate", "2022-04-07")
                                        .add("hearingDateWelsh", "7 Ebrill 2022")
                                        .add("courtRooms", createArrayBuilder()
                                                .add(createObjectBuilder()
                                                        .add("courtRoomName", "Courtroom 01")
                                                        .add("welshCourtRoomName", "Courtroom 01")
                                                        .add("judiciaryNames", "Miss Atkins JP, Mrs Colman, District Judge (MC) Daber, Mr Hartopp JP, Mrs Maule-Farrell JP, Mr Rahman JP")
                                                        .add("welshJudiciaryNames", "Atkins, Mrs  Colman,   Daber, Mr  Hartopp YH, Mrs  Maule-Farrell YH, Mr  Rahman YH")
                                                        .add("timeslots", createArrayBuilder().add(
                                                                createObjectBuilder().add("hearing", createArrayBuilder()
                                                                        .add(createObjectBuilder()
                                                                                .add("id", "1a0f21ad-d9dc-4e42-9fb3-da2c5de0d7c2")
                                                                                .add("sequence", "1")
                                                                                .add("hearingType", "First hearing")
                                                                                .build()
                                                                        ).build())
                                                                ).build()
                                                        ).build()
                                                ).build()
                                        ).build()).build())
                                .build())
                .build();
    }


    private JsonEnvelope createCommandEnvelope(final JsonObject payload, final String commandName) {
        final UUID uuid = randomUUID();
        final UUID userId = randomUUID();

        final Metadata metadata = Envelope
                .metadataBuilder()
                .withName(commandName)
                .withId(uuid)
                .withUserId(userId.toString())
                .build();
        return new DefaultJsonEnvelopeProvider().envelopeFrom(metadata, payload);
    }

    private void verifySubscriberHandlerResults(String eventName, final String jsonPath, final String expectedValue) throws EventStreamException {
        final Stream<JsonEnvelope> envelopeStream = verifyAppendAndGetArgumentFrom(eventStream);

        assertThat(envelopeStream, streamContaining(
                jsonEnvelope(
                        metadata()
                                .withName(eventName),
                        payload().isJson(allOf(
                                withJsonPath(jsonPath, Matchers.is(expectedValue))
                                )
                        ))

                )
        );
    }

}