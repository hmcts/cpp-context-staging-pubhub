package uk.gov.moj.cpp.staging.pubhub.event.processor;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.CROWN_LCSU;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.MAGS_STANDARD_LIST_ENGLISH;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.PressTransparencyReportGenerated;
import uk.gov.justice.staging.pubhub.PublicReportGenerated;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.json.schema.PublishLiveStatus;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.moj.cpp.staging.pubhub.event.service.PublishingService;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.LcsPublishingHubTransformer;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.PublishingHubTransformer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.SjpPublishingHubTransformer;

@SuppressWarnings({"squid:S3655"})
@ServiceComponent(EVENT_PROCESSOR)
public class PublishEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishEventProcessor.class);
    private static final String EVENT_PAYLOAD_DEBUG_STRING = "Received '{}' event with payload {}";

    @Inject
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private PublishingHubTransformer publishingHubTransformer;

    @Inject
    private LcsPublishingHubTransformer lcsPublishingHubTransformer;

    @Inject
    private PublishingService publishingService;

    @Inject
    private SjpPublishingHubTransformer sjpPublishingHubTransformer;

    @Handles("stagingpubhub.event.publish-requested")
    public void publishRequested(final JsonEnvelope envelope) {

        final PublishRequested publishRequested = jsonObjectConverter.convert(envelope.payloadAsJsonObject(), PublishRequested.class);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_PAYLOAD_DEBUG_STRING, "stagingpubhub.event.publish-requested", publishRequested.toString());
        }

        final PubhubMaster publishStandardCourtList = publishingHubTransformer.transformStandardList(publishRequested);
        if (nonNull(publishStandardCourtList)) {
            final String transformedJsonPayload = objectToJsonObjectConverter.convert(publishStandardCourtList).toString();
            LOGGER.info("Standard court list payload send to P&I {}", transformedJsonPayload);
            final String courtId = publishRequested.getStandardList().getCourtId();
            publishingService.sendData(transformedJsonPayload, MAGS_STANDARD_LIST_ENGLISH.getValue(), envelope, courtId);
        }
    }

    @Handles("stagingpubhub.event.publish-live-status")
    public void publishLiveStatus(final JsonEnvelope envelope){
        final PublishLiveStatus publishLiveStatus = jsonObjectConverter.convert(envelope.payloadAsJsonObject(), PublishLiveStatus.class);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_PAYLOAD_DEBUG_STRING, "stagingpubhub.event.publish-live-status", publishLiveStatus.toString());
        }

        final PubhubMaster liveCaseStatus = lcsPublishingHubTransformer.transformLcsu(publishLiveStatus);
        if(nonNull(liveCaseStatus)) {
            final String transformedJsonPayload = objectToJsonObjectConverter.convert(liveCaseStatus).toString();
            LOGGER.info("Live case status payload send to P&I {}", transformedJsonPayload);
            final String courtId = publishLiveStatus.getCourtId();
            publishingService.sendData(transformedJsonPayload, CROWN_LCSU.getValue(), envelope, courtId);
        }

    }

    @Handles("stagingpubhub.event.sjp-press-published")
    public void publishSjpPublicReportRequested(final JsonEnvelope envelope) {

        final PressTransparencyReportGenerated publishRequested = jsonObjectConverter.convert(envelope.payloadAsJsonObject(), PressTransparencyReportGenerated.class);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_PAYLOAD_DEBUG_STRING, "stagingpubhub.event.sjp-press-published", publishRequested.toString());
        }

        transformAndSendTpPAndI(envelope, DocumentType.SJP_PRESS_LIST);

    }

    @Handles("stagingpubhub.event.sjp-public-published")
    public void publishSjpPressReportRequested(final JsonEnvelope envelope) {

        final PublicReportGenerated publishRequested = jsonObjectConverter.convert(envelope.payloadAsJsonObject(), PublicReportGenerated.class);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_PAYLOAD_DEBUG_STRING, "stagingpubhub.event.sjp-public-published", publishRequested.toString());
        }

        transformAndSendTpPAndI(envelope, DocumentType.SJP_PUBLIC_LIST);

    }

    private void transformAndSendTpPAndI(JsonEnvelope envelope, DocumentType documentType) {
        final PubhubMaster publishSjpPressList = sjpPublishingHubTransformer.transformSjpList(envelope, documentType);
        if (nonNull(publishSjpPressList)) {
            final String transformedJsonPayload = objectToJsonObjectConverter.convert(publishSjpPressList).toString();
            LOGGER.info("SJP list payload send to P&I {}", transformedJsonPayload);
            publishingService.sendData(transformedJsonPayload, documentType.getValue(), envelope, "SJP");
        }
    }
}
