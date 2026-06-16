package uk.gov.moj.cpp.staging.pubhub.event.processor;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.featurecontrol.FeatureControlGuard;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventProcessor.class);
    private static final String PUBLIC_EVENT_LIVE_STATUS_PUBLISHED = "public.hearing.live-status-published";
    private static final String STAGING_PUBHUB_COMMAND_LIVE_STATUS = "stagingpubhub.command.handler.live-status-published";

    @Inject
    @ServiceComponent(EVENT_PROCESSOR)
    private Sender sender;

    @Inject
    private FeatureControlGuard featureControlGuard;

    @Handles(PUBLIC_EVENT_LIVE_STATUS_PUBLISHED)
    public void handleLiveStatusPublishedPublicEvent(final JsonEnvelope jsonEnvelope) {
        final JsonObject payload = jsonEnvelope.payloadAsJsonObject();
        if (featureControlGuard.isFeatureEnabled("PUBHUB")) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} event received with payload {}", PUBLIC_EVENT_LIVE_STATUS_PUBLISHED, payload);
            }

            sender.send(envelopeFrom(metadataFrom(jsonEnvelope.metadata()).withName(STAGING_PUBHUB_COMMAND_LIVE_STATUS),
                    payload));
        } else {
            LOGGER.info("Feature is not enabled {}", PUBLIC_EVENT_LIVE_STATUS_PUBLISHED);
        }
    }
}
