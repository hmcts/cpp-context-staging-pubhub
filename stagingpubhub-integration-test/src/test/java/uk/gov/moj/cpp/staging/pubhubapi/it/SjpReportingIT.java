package uk.gov.moj.cpp.staging.pubhubapi.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.moj.cpp.staging.pubhubapi.stub.PublishingServiceStub.verifyPublicationV2Api;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.FeatureToggleUtil.enablePubHubFeature;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.FileUtil.getPayload;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.staging.pubhub.PressTransparencyReportGenerated;
import uk.gov.moj.cpp.staging.pubhubapi.utils.AbstractTestHelper;
import uk.gov.moj.cpp.staging.pubhubapi.utils.QueueUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class SjpReportingIT extends AbstractTestHelper {

    public static final String PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED = "public.sjp.press-transparency-report-generated";

    private MessageProducer messageProducerClientPublic;
    private MessageConsumer publicEventsForSjpPressReportConsumer;

    protected static final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    private final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();
    protected final JsonObjectToObjectConverter jsonToObjectConverter = new JsonObjectToObjectConverter(objectMapper);


    @Before
    public void setUp() {
        messageProducerClientPublic = QueueUtil.publicEvents.createProducer();
        publicEventsForSjpPressReportConsumer = QueueUtil.publicEvents.createConsumer(PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED);
    }

    @Test
    public void shouldRaisePublishedEventWhenSjpPressListPublicEventConsumed() throws IOException {
        enablePubHubFeature(true);

        final String payload = getPayload("stub-data/public.sjp.press-transparency-report-generated.json");

        stubFor(post(urlPathEqualTo("publishing-hub/v2/publication"))
                .withRequestBody(equalToJson(getPayload("stub-data/press_list_payload_sent.json")))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("Ocp-Apim-Subscription-Key", "3674a16507104b749a76b29b6c837352")
                        .withHeader("Ocp-Apim-Trace", "true")));

        sendMessage(messageProducerClientPublic, PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED, stringToJsonObjectConverter.convert(payload), metadataBuilder()
                .withId(randomUUID())
                .withName(PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED)
                .withUserId(randomUUID().toString())
                .build());
        assertNotNull(doVerifySjpPublicEvent(publicEventsForSjpPressReportConsumer));

        final MessageConsumer consumer = QueueUtil.privateEvents.createConsumer("stagingpubhub.event.sjp-press-published");

        Optional<JsonObject> jsonObject = QueueUtil.retrieveMessageAsJsonObject(consumer);

        final PressTransparencyReportGenerated pressTransparencyReportGenerated = jsonToObjectConverter.convert(jsonObject.get(), PressTransparencyReportGenerated.class);
        assertThat(pressTransparencyReportGenerated.getLanguage().toString(), equalTo("ENGLISH"));
        assertNotNull(pressTransparencyReportGenerated.getListPayload());

        final List<String> expectedDetails = newArrayList("SJP Press list");
        verifyPublicationV2Api(expectedDetails);
    }

    private String doVerifySjpPublicEvent(final MessageConsumer publicEventConsumer) {
        final Optional<JsonObject> message = QueueUtil.retrieveMessageAsJsonObject(publicEventConsumer);
        final JsonObject sjpPublicEvent = message.get();
        return sjpPublicEvent.getJsonObject("listPayload").getString("generatedDateAndTime");
    }

    public void sendMessage(final MessageProducer messageProducer, final String eventName, final JsonObject payload, final Metadata metadata) {

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, payload);
        final String json = jsonEnvelope.toDebugStringPrettyPrint();
        try {
            final TextMessage message = new ActiveMQTextMessage();

            message.setText(json);
            message.setStringProperty("CPPNAME", eventName);

            messageProducer.send(message);
        } catch (final JMSException e) {
            throw new RuntimeException("Failed to send message. commandName: '" + eventName + "', json: " + json, e);
        }
    }

    @After
    public void tearDown() throws Exception {
        messageProducerClientPublic.close();
    }
}
