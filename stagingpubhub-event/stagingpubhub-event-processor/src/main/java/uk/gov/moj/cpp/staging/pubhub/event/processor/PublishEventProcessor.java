package uk.gov.moj.cpp.staging.pubhub.event.processor;

import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.CROWN_LCSU;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.MAGS_STANDARD_LIST_ENGLISH;
import static uk.gov.moj.cpp.staging.pubhub.event.util.JsonUtil.renameKey;
import static uk.gov.moj.cpp.staging.pubhub.helper.RetryHelper.retryHelper;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.RequestType;
import uk.gov.justice.staging.pubhub.Language;
import uk.gov.justice.staging.pubhub.PressTransparencyReportGenerated;
import uk.gov.justice.staging.pubhub.PublicReportGenerated;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.json.schema.PublishLiveStatus;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.moj.cpp.staging.pubhub.event.service.ApplicationParameters;
import uk.gov.moj.cpp.staging.pubhub.event.service.PublishingService;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.LcsPublishingHubTransformer;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.PublishingHubTransformer;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.SjpPublishingHubTransformer;
import uk.gov.moj.cpp.staging.pubhub.exception.AzureAPIMInvocationException;

import java.io.IOException;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
@SuppressWarnings({"squid:S00112", "squid:S3655"})
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

    @Inject
    private ApplicationParameters applicationParameters;


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
            final String courtId = nonNull(publishRequested.getStandardList().getCourtId()) ?  publishRequested.getStandardList().getCourtId() : "0";
            publishingService.sendData(transformedJsonPayload, MAGS_STANDARD_LIST_ENGLISH.getValue(), envelope, courtId, Language.ENGLISH.toString());
        }
    }

    @Handles("stagingpubhub.event.publish-live-status")
    public void publishLiveStatus(final JsonEnvelope envelope) {
        final PublishLiveStatus publishLiveStatus = jsonObjectConverter.convert(envelope.payloadAsJsonObject(), PublishLiveStatus.class);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_PAYLOAD_DEBUG_STRING, "stagingpubhub.event.publish-live-status", publishLiveStatus.toString());
        }

        final PubhubMaster liveCaseStatus = lcsPublishingHubTransformer.transformLcsu(publishLiveStatus);
        if (nonNull(liveCaseStatus)) {
            final String transformedJsonPayload = objectToJsonObjectConverter.convert(liveCaseStatus).toString();
            LOGGER.info("Live case status payload send to P&I {}", transformedJsonPayload);
            final String courtId = nonNull(publishLiveStatus.getCourtId()) ? publishLiveStatus.getCourtId() : "0";
            publishingService.sendData(transformedJsonPayload, CROWN_LCSU.getValue(), envelope, courtId, Language.ENGLISH.toString());
        }

    }

    @Handles("stagingpubhub.event.sjp-press-published")
    public void publishSjpPressReportRequested(final JsonEnvelope envelope) throws Exception {

        final PressTransparencyReportGenerated publishRequested = jsonObjectConverter.convert(envelope.payloadAsJsonObject(), PressTransparencyReportGenerated.class);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_PAYLOAD_DEBUG_STRING, "stagingpubhub.event.sjp-press-published", publishRequested.toString());
        }

        final DocumentType documentType =  publishRequested.getRequestType() == RequestType.FULL ? DocumentType.SJP_PRESS_LIST : DocumentType.SJP_DELTA_PRESS_LIST;

        final PubhubMaster publishSjpPressList = sjpPublishingHubTransformer.transformSjpList(envelope, documentType);

        if (nonNull(publishSjpPressList)) {
            final String payload = transformPayload(publishSjpPressList);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("SJP press list sent to CATH: {}", payload);
            }

            retryHelper()
                    .withSupplier(() -> publishingService.sendData(payload, publishingService.fetchMetaData(documentType.getValue(), envelope, "0", publishRequested.getLanguage().toString())))
                    .withApimUrl(applicationParameters.getPublishingHubUrlV2())
                    .withPayload(payload)
                    .withRetryTimes(parseInt(applicationParameters.getRetryTimes()))
                    .withRetryInterval(parseInt(applicationParameters.getRetryInterval()))
                    .withExceptionSupplier(() -> new AzureAPIMInvocationException(documentType.getValue(), applicationParameters.getPublishingHubUrlV2()))
                    .withPredicate(statusCode -> statusCode > 429)
                    .build()
                    .postWithRetry();
        }
    }

    @Handles("stagingpubhub.event.sjp-public-published")
    public void publishSjpPublicReportRequested(final JsonEnvelope envelope) throws InterruptedException {

        final PublicReportGenerated publishRequested = jsonObjectConverter.convert(envelope.payloadAsJsonObject(), PublicReportGenerated.class);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_PAYLOAD_DEBUG_STRING, "stagingpubhub.event.sjp-public-published", publishRequested.toString());
        }

        final DocumentType documentType = publishRequested.getRequestType() == RequestType.FULL ? DocumentType.SJP_PUBLIC_LIST : DocumentType.SJP_DELTA_PUBLIC_LIST;

        final PubhubMaster publishSjpPublicList = sjpPublishingHubTransformer.transformSjpList(envelope, documentType);

        if (nonNull(publishSjpPublicList)) {
            final String payload = objectToJsonObjectConverter.convert(publishSjpPublicList).toString();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("SJP public list sent to CATH: {}", payload);
            }

            retryHelper()
                    .withSupplier(() -> publishingService.sendData(payload, publishingService.fetchMetaData(documentType.getValue(), envelope, "0", publishRequested.getLanguage().toString())))
                    .withApimUrl(applicationParameters.getPublishingHubUrlV2())
                    .withPayload(payload)
                    .withRetryTimes(parseInt(applicationParameters.getRetryTimes()))
                    .withRetryInterval(parseInt(applicationParameters.getRetryInterval()))
                    .withExceptionSupplier(() -> new AzureAPIMInvocationException(documentType.getValue(), applicationParameters.getPublishingHubUrlV2()))
                    .withPredicate(statusCode -> statusCode > 429)
                    .build()
                    .postWithRetry();
        }
    }

    private String transformPayload(final PubhubMaster pubhubMaster) throws IOException {
        final String transformedJsonPayload = objectToJsonObjectConverter.convert(pubhubMaster).toString();
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode rootNode = objectMapper.readTree(transformedJsonPayload);
        renameKey((ObjectNode) rootNode, "cases", "case");
        return objectMapper.writeValueAsString(rootNode);
    }
}
