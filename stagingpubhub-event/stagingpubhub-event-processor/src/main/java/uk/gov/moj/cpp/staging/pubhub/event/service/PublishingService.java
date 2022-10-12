package uk.gov.moj.cpp.staging.pubhub.event.service;

import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.CROWN_LCSU;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.MAGS_STANDARD_LIST_ENGLISH;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.SJP_PRESS_LIST;
import static uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType.SJP_PUBLIC_LIST;

import uk.gov.justice.services.common.configuration.Value;
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
    private static final String CONFIDENTIAL = "CONFIDENTIAL";
    private static final String ENGLISH = "ENGLISH";

    //STE Mock URL - https://spnl-apim-int-gw.cpp.nonlive/publishing-hub/publication
    @Inject
    @Value(key = "publishingHubUrl", defaultValue = "http://localhost:8080/publishing-hub/publication")
    private String publishingHubUrl;

    @Inject
    @Value(key = "publishingHub.subscription.key", defaultValue = "3674a16507104b749a76b29b6c837352")
    private String subscriptionKey;

    @Inject
    private RestEasyClientService restEasyClientService;

    @Inject
    private ReferenceDataService referenceDataService;

    public void sendData(final String payload) {
        final Response response = restEasyClientService.post(publishingHubUrl, payload, subscriptionKey);
        LOGGER.info("APIM {} called with Request: {} and received status response: {}", publishingHubUrl, payload, response.getStatus());
    }

    public void sendData(final String payload, final String listType, final JsonEnvelope envelope, final String courtId) {
        final Response response = restEasyClientService.post(publishingHubUrl, payload, subscriptionKey, fetchMetaData(listType, envelope, courtId));
        LOGGER.info("APIM {} called with Request: {} and received status response: {}", publishingHubUrl, payload, response.getStatus());
    }

    private Meta fetchMetaData(final String documentType, final JsonEnvelope envelope, final String courtId) {
        LOGGER.info("Header information:");
        Meta meta = null;
        try {
            meta = referenceDataService.getMetadata(documentType, envelope);
        } catch (Exception exception) {
            //Harcoded for testing purpose
            final Meta.Builder builder = Meta.meta()
                    .withProvenance(SourceSystem.COMMON_PLATFORM.getValue())
                    .withCourtId(courtId)
                    .withContentDate(ZonedDateTime.now())
                    .withSensitivity(CONFIDENTIAL)
                    .withLanguage(ENGLISH)
                    .withDisplayFrom(ZonedDateTime.now())
                    .withDisplayTo(ZonedDateTime.now().plusHours(24));
            if (documentType.equals(MAGS_STANDARD_LIST_ENGLISH.getValue())) {
                builder.withType(ArtefactType.LIST.getValue())
                        .withListType(ListType.MAGS_STANDARD_LIST.getValue());
            } else if (documentType.equals(CROWN_LCSU.getValue())) {
                builder.withType(ArtefactType.LCSU.getValue())
                        .withListType(ListType.CROWN_LCSU.getValue());
            }
            else if(documentType.equals(SJP_PUBLIC_LIST.getValue())){
                builder.withType(ArtefactType.LIST.getValue())
                        .withListType(ListType.SJP_PUBLIC_LIST.getValue());
            }
            else if(documentType.equals(SJP_PRESS_LIST.getValue())){
                builder.withType(ArtefactType.LIST.getValue())
                        .withListType(ListType.SJP_PRESS_LIST.getValue());
            }
            meta = builder.build();
        }
        LOGGER.info(meta.toString());
        return meta;
    }
}