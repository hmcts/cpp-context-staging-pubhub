package uk.gov.moj.cpp.staging.pubhub.event.service;

import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.CROWN_LCSU;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.MAGS_STANDARD_LIST_ENGLISH;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.SJP_DELTA_PRESS_LIST;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.SJP_PRESS_LIST;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.SJP_PUBLIC_LIST;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.SJP_DELTA_PUBLIC_LIST;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.json.schema.Meta;

import java.time.ZonedDateTime;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1166", "squid:S2221", "squid:S2629"})
public class PublishingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingService.class);
    private static final String APIM_LOGGER = "APIM {} called and received status response: {}";

    @Inject
    private ApplicationParameters applicationParameters;

    @Inject
    private RestEasyClientService restEasyClientService;

    @Inject
    private ReferenceDataService referenceDataService;

    @Inject
    private AzureIdentityService azureIdentityService;

    public void sendData(final String payload) {
        final Response response = restEasyClientService.post(applicationParameters.getPublishingHubUrl(), payload, applicationParameters.getSubscriptionKey());
        LOGGER.info(APIM_LOGGER, applicationParameters.getPublishingHubUrl(), response.getStatus());
    }

    public void sendData(final String payload, final String listType, final JsonEnvelope envelope, final String courtId, final String language) {
        final Response response = restEasyClientService.post(applicationParameters.getPublishingHubUrl(), payload, applicationParameters.getSubscriptionKey(), fetchMetaData(listType, envelope, courtId, language));
        LOGGER.info(APIM_LOGGER, applicationParameters.getPublishingHubUrl(), response.getStatus());
    }

    public Integer sendData(final String payload, final Meta metadata) {
        final Response response = restEasyClientService.post(applicationParameters.getPublishingHubUrlV2(), payload, azureIdentityService.getTokenFromLocalClientSecretCredentials(), azureIdentityService.getTokenFromRemoteClientSecretCredentials(), metadata);
        LOGGER.info(APIM_LOGGER, applicationParameters.getPublishingHubUrlV2(), response.getStatus());
        return response.getStatus();
    }

    public Meta fetchMetaData(final String documentType, final JsonEnvelope envelope, final String courtId, final String language) {
        LOGGER.info("Header information:");
        Meta meta = null;
        try {
            meta = referenceDataService.getMetadata(documentType, envelope);
        } catch (Exception exception) {
            //Default values
            final Meta.Builder builder = Meta.meta()
                    .withProvenance(SourceSystem.COMMON_PLATFORM.getValue())
                    .withCourtId(courtId)
                    .withContentDate(ZonedDateTime.now())
                    .withSensitivity(Sensitivity.PUBLIC.getValue())
                    .withLanguage(language)
                    .withDisplayFrom(ZonedDateTime.now())
                    .withDisplayTo(ZonedDateTime.now().plusHours(24));
            if (documentType.equals(MAGS_STANDARD_LIST_ENGLISH.getValue())) {
                builder.withType(ArtefactType.LIST.getValue())
                        .withListType(ListType.MAGS_STANDARD_LIST.toString());
            } else if (documentType.equals(CROWN_LCSU.getValue())) {
                builder.withType(ArtefactType.LCSU.getValue())
                        .withListType(ListType.CROWN_LCSU.toString());
            } else if (documentType.equals(SJP_PUBLIC_LIST.getValue())) {
                builder.withType(ArtefactType.LIST.getValue())
                        .withListType(ListType.SJP_PUBLIC_LIST.toString());
            } else if (documentType.equals(SJP_DELTA_PUBLIC_LIST.getValue())) {
                builder.withType(ArtefactType.LIST.getValue())
                        .withListType(ListType.SJP_DELTA_PUBLIC_LIST.toString());
            } else if (documentType.equals(SJP_PRESS_LIST.getValue())) {
                builder.withType(ArtefactType.LIST.getValue())
                        .withSensitivity(Sensitivity.CLASSIFIED.getValue())
                        .withListType(ListType.SJP_PRESS_LIST.toString());
            } else if (documentType.equals(SJP_DELTA_PRESS_LIST.getValue())) {
                builder.withType(ArtefactType.LIST.getValue())
                        .withSensitivity(Sensitivity.CLASSIFIED.getValue())
                        .withListType(ListType.SJP_DELTA_PRESS_LIST.toString());
            }
            meta = builder.build();
        }
        LOGGER.info(meta.toString());
        return meta;
    }
}