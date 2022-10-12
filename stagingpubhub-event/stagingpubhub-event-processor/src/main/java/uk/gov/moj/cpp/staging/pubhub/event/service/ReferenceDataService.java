package uk.gov.moj.cpp.staging.pubhub.event.service;


import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.json.schema.Meta;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataService.class);
    private static final String REFERENCEDATA_QUERY_METADATA = "";
    private static final String REFERENCEDATA_QUERY_COURTROOM = "referencedata.query.courtroom";

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    @ServiceComponent(COMMAND_API)
    private Requester requester;

    public Meta getMetadata(final String documentType, final JsonEnvelope event) {
        final JsonObject payload = createObjectBuilder().add("documentType", documentType).build();
        LOGGER.info("'referencedata.query' request with payload {}", payload);

        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(payload)
                .withName(REFERENCEDATA_QUERY_METADATA)
                .withMetadataFrom(event);

        return convertToMetaObject(requester.request(requestEnvelope));
    }

    private Meta convertToMetaObject(final JsonEnvelope jsonEnvelope){
        return jsonObjectConverter.convert(jsonEnvelope.payloadAsJsonObject(), Meta.class);
    }

    public JsonEnvelope getCourtCentreById(final UUID courtCentreId, final JsonEnvelope event) {
        final JsonObject payload = createObjectBuilder().add("id", courtCentreId.toString()).build();
        LOGGER.info("'referencedata.query.courtroom' request with payload {}", payload);

        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(payload)
                .withName(REFERENCEDATA_QUERY_COURTROOM)
                .withMetadataFrom(event);

        return requester.request(envelopeFrom(requestEnvelope.metadata(), requestEnvelope.payload()));
    }

    public String getOucodeFromEnvelope(final UUID courtCentreId, final JsonEnvelope event){
        final JsonEnvelope jsonEnvelope = getCourtCentreById(courtCentreId, event);
        final JsonObject organisationUnitJsonObject = jsonEnvelope.payloadAsJsonObject();
        return organisationUnitJsonObject.getString("oucode");
    }
}
