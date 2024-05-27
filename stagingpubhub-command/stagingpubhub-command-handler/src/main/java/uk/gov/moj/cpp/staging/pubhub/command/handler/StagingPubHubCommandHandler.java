package uk.gov.moj.cpp.staging.pubhub.command.handler;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.enveloper.Enveloper.toEnvelopeWithMetadataFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.ListPayload;
import uk.gov.justice.staging.pubhub.StandardList;
import uk.gov.justice.staging.pubhub.json.schema.LiveStatusPublished;
import uk.gov.moj.cpp.staging.pubhub.domain.PubHubAggregate;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonValue;


@ServiceComponent(COMMAND_HANDLER)
public class StagingPubHubCommandHandler {
    public static final String STAGINGPUBHUB_COMMAND_HANDLER_PUBLISH_STANDARD_LIST = "stagingpubhub.command.handler.publish-standard-list";
    public static final String STAGINGPUBHUB_COMMAND_HANDLER_LIVE_STATUS_PUBLISHED = "stagingpubhub.command.handler.live-status-published";
    public static final String STAGINGPUBHUB_COMMAND_HANDLER_SJP_PRESS_PUBLISHED = "stagingpubhub.command.handler.sjp-press-published";
    public static final String STAGINGPUBHUB_COMMAND_HANDLER_SJP_PUBLIC_PUBLISHED = "stagingpubhub.command.handler.sjp-public-published";

    @Inject
    private EventSource eventSource;
    @Inject
    private AggregateService aggregateService;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles(STAGINGPUBHUB_COMMAND_HANDLER_PUBLISH_STANDARD_LIST)
    public void handlePublishStandard(final JsonEnvelope envelope) throws EventStreamException {
        final StandardList payload = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject().getJsonObject("standardList"), StandardList.class);
        final EventStream eventStream = eventSource.getStreamById(randomUUID());
        final PubHubAggregate pubHubAggregate = aggregateService.get(eventStream, PubHubAggregate.class);

        final Stream<Object> events = pubHubAggregate.requestPublish(payload);

        appendEventsToStream(envelope, eventStream, events);
    }

    @Handles(STAGINGPUBHUB_COMMAND_HANDLER_LIVE_STATUS_PUBLISHED)
    public void handleLiveStatusPublished(final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = eventSource.getStreamById(randomUUID());
        final PubHubAggregate pubHubAggregate = aggregateService.get(eventStream, PubHubAggregate.class);
        final LiveStatusPublished liveStatusPublished = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), LiveStatusPublished.class);
        final Stream<Object> events = pubHubAggregate.publishLiveStatus(liveStatusPublished.getDocumentName(), liveStatusPublished.getDocumentDate(),
                liveStatusPublished.getVersion(), liveStatusPublished.getVenueId(), liveStatusPublished.getVenueType(), liveStatusPublished.getListType(),
                liveStatusPublished.getCourtCentreName(), liveStatusPublished.getCourtId(), liveStatusPublished.getAddress1(), liveStatusPublished.getPostCode(),
                liveStatusPublished.getHearingDate(), liveStatusPublished.getCourtRooms());

        appendEventsToStream(envelope, eventStream, events);
    }

    @Handles(STAGINGPUBHUB_COMMAND_HANDLER_SJP_PRESS_PUBLISHED)
    public void handleSjpPressReportPublished(final JsonEnvelope jsonEnvelope) throws EventStreamException {
        final ListPayload payload = jsonObjectToObjectConverter.convert(jsonEnvelope.payloadAsJsonObject().getJsonObject("listPayload"), ListPayload.class);
        final EventStream eventStream = eventSource.getStreamById(randomUUID());
        final PubHubAggregate pubHubAggregate = aggregateService.get(eventStream, PubHubAggregate.class);

        final Stream<Object> events = pubHubAggregate.requestSjpPressPublish(jsonEnvelope.payloadAsJsonObject().getString("language"), payload, jsonEnvelope.payloadAsJsonObject().getString("requestType"));

        appendEventsToStream(jsonEnvelope, eventStream, events);
    }

    @Handles(STAGINGPUBHUB_COMMAND_HANDLER_SJP_PUBLIC_PUBLISHED)
    public void handleSjpPublicReportPublished(final JsonEnvelope jsonEnvelope) throws EventStreamException {
        final ListPayload payload = jsonObjectToObjectConverter.convert(jsonEnvelope.payloadAsJsonObject().getJsonObject("listPayload"), ListPayload.class);
        final EventStream eventStream = eventSource.getStreamById(randomUUID());
        final PubHubAggregate pubHubAggregate = aggregateService.get(eventStream, PubHubAggregate.class);

        final Stream<Object> events = pubHubAggregate.requestSjpPublicPublish(jsonEnvelope.payloadAsJsonObject().getString("language"), payload, jsonEnvelope.payloadAsJsonObject().getString("requestType"));

        appendEventsToStream(jsonEnvelope, eventStream, events);
    }

    private void appendEventsToStream(final Envelope<?> envelope, final EventStream eventStream, final Stream<Object> events) throws EventStreamException {
        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(envelope.metadata(), JsonValue.NULL);
        eventStream.append(events.map(toEnvelopeWithMetadataFrom(jsonEnvelope)));
    }

}
