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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class SJPListReportRequestedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SJPListReportRequestedProcessor.class);

    public static final String PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED = "public.sjp.press-transparency-report-generated";
    public static final String PUBLIC_SJP_PUBLIC_CASES_LIST_REPORT_GENERATED = "public.sjp.pending-cases-public-list-generated";
    private static final String STAGING_PUBHUB_COMMAND_SJP_PRESS = "stagingpubhub.command.handler.sjp-press-published";
    private static final String STAGING_PUBHUB_COMMAND_SJP_PUBLIC = "stagingpubhub.command.handler.sjp-public-published";

    @Inject
    @ServiceComponent(EVENT_PROCESSOR)
    private Sender sender;

    @Inject
    private FeatureControlGuard featureControlGuard;

    @Handles(PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED)
    public void publishSjpTransparencyPressReportRequested(final JsonEnvelope envelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED {}", envelope.toObfuscatedDebugString());
        }

        sender.send(envelopeFrom(metadataFrom(envelope.metadata()).withName(STAGING_PUBHUB_COMMAND_SJP_PRESS),
                envelope.payloadAsJsonObject()));
    }

    @Handles(PUBLIC_SJP_PUBLIC_CASES_LIST_REPORT_GENERATED)
    public void publishSjpPublicReportRequested(final JsonEnvelope envelope) {
        if (featureControlGuard.isFeatureEnabled("PUBHUB")) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("PUBLIC_SJP_PUBLIC_REPORT_GENERATED {}", envelope.toObfuscatedDebugString());
            }

            sender.send(envelopeFrom(metadataFrom(envelope.metadata()).withName(STAGING_PUBHUB_COMMAND_SJP_PUBLIC),
                    envelope.payloadAsJsonObject()));
        }
    }

}