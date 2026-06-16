package uk.gov.moj.cpp.staging.pubhubapi.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.UUID.randomUUID;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.FeatureToggleUtil.enablePubHubFeature;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.FileUtil.getPayload;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.justice.staging.pubhub.PressTransparencyReportGenerated;
import uk.gov.justice.staging.pubhub.PublicReportGenerated;
import uk.gov.moj.cpp.staging.pubhubapi.utils.AbstractTestHelper;
import uk.gov.moj.cpp.staging.pubhubapi.utils.QueueUtil;

import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"squid:S1607"})
public class SjpReportingIT extends AbstractTestHelper {

    public static final String PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED = "public.sjp.press-transparency-report-generated";
    public static final String PUBLIC_SJP_PENDING_CASES_PUBLIC_LIST_GENERATED = "public.sjp.pending-cases-public-list-generated";

    private MessageProducer messageProducerClientPublic;
    private MessageConsumer publicEventsForSjpPressReportConsumer;
    private MessageConsumer publicEventsForSjpPendingReportConsumer;

    protected static final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    private final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();
    protected JsonObjectToObjectConverter jsonToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();


    @BeforeEach
    public void setUp() {
        messageProducerClientPublic = QueueUtil.publicEvents.createProducer();
        publicEventsForSjpPressReportConsumer = QueueUtil.publicEvents.createConsumer(PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED);
        publicEventsForSjpPendingReportConsumer = QueueUtil.publicEvents.createConsumer(PUBLIC_SJP_PENDING_CASES_PUBLIC_LIST_GENERATED);
    }

    @Test
    public void shouldRaisePublishedEventWhenSjpPressListPublicEventConsumed() {
        enablePubHubFeature(true);

        final String payload = getPayload("stub-data/public.sjp.press-transparency-report-generated.json");

        final String subscriptionKey = System.getenv("PUBLISHING_HUB_SUBSCRIPTION_KEY");
        stubFor(post(urlPathEqualTo("publishing-hub/v2/publication"))
                .withRequestBody(equalToJson(getPayload("stub-data/press_list_payload_sent.json")))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("Ocp-Apim-Subscription-Key", subscriptionKey != null ? subscriptionKey : "test-key")
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
    }

    @Test
    public void shouldRaisePublishedEventWhenSjpPendingListPublicEventConsumed() {
        enablePubHubFeature(true);

        final String payload = getPayload("stub-data/public.sjp.pending-cases-public-list-generated.json");

        final String subscriptionKey = System.getenv("PUBLISHING_HUB_SUBSCRIPTION_KEY");
        stubFor(post(urlPathEqualTo("publishing-hub/v2/publication"))
                .withRequestBody(equalToJson(getPayload("stub-data/public_list_payload_sent.json")))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("Ocp-Apim-Subscription-Key", subscriptionKey != null ? subscriptionKey : "test-key")
                        .withHeader("Ocp-Apim-Trace", "true")));

        sendMessage(messageProducerClientPublic, PUBLIC_SJP_PENDING_CASES_PUBLIC_LIST_GENERATED, stringToJsonObjectConverter.convert(payload), metadataBuilder()
                .withId(randomUUID())
                .withName(PUBLIC_SJP_PENDING_CASES_PUBLIC_LIST_GENERATED)
                .withUserId(randomUUID().toString())
                .build());
        assertNotNull(doVerifySjpPublicEvent(publicEventsForSjpPendingReportConsumer));

        final MessageConsumer consumer = QueueUtil.privateEvents.createConsumer("stagingpubhub.event.sjp-public-published");

        Optional<JsonObject> jsonObject = QueueUtil.retrieveMessageAsJsonObject(consumer);

        final PublicReportGenerated publicReportGenerated = jsonToObjectConverter.convert(jsonObject.get(), PublicReportGenerated.class);
        assertThat(publicReportGenerated.getLanguage().toString(), equalTo("ENGLISH"));
        assertNotNull(publicReportGenerated.getListPayload());
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

    @AfterEach
    public void tearDown() throws Exception {
        messageProducerClientPublic.close();
    }
}
