package uk.gov.moj.cpp.staging.pubhub;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.FeatureControl;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_API)
public class StagingPubHubCommandApi {

    @Inject
    private Sender sender;

    @Inject
    private JsonSchemaValidator jsonSchemaValidator;

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingPubHubCommandApi.class);

    @FeatureControl("PUBHUB")
    @Handles("stagingpubhub.command.publish-standard-list")
    public void publishStandard(final JsonEnvelope envelope) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Received request {} {}", "stagingpubhub.command.publish-standard-list", envelope.asJsonObject());
        }
        sender.send(envelop(envelope.payloadAsJsonObject())
                .withName("stagingpubhub.command.handler.publish-standard-list")
                .withMetadataFrom(envelope));
    }
}
