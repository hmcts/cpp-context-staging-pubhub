package uk.gov.moj.cpp.staging.pubhubapi.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.moj.cpp.staging.pubhubapi.stub.PublishingServiceStub.verifyPublicationApi;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.FeatureToggleUtil.enablePubHubFeature;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.staging.pubhub.PressTransparencyReportGenerated;
import uk.gov.justice.staging.pubhub.PublicReportGenerated;
import uk.gov.moj.cpp.staging.pubhubapi.utils.AbstractTestHelper;
import uk.gov.moj.cpp.staging.pubhubapi.utils.FileUtil;
import uk.gov.moj.cpp.staging.pubhubapi.utils.QueueUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class SjpReportingIT extends AbstractTestHelper {

    public static final String PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED = "public.sjp.press-transparency-report-generated";
    public static final String PUBLIC_SJP_PENDING_CASES_PUBLIC_LIST_GENERATED = "public.sjp.pending-cases-public-list-generated";

    private MessageProducer messageProducerClientPublic;
    private MessageConsumer publicEventsForSjpPressReportConsumer;
    private MessageConsumer publicEventsForSjpPendingReportConsumer;

    protected static final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    private final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();
    protected  final JsonObjectToObjectConverter jsonToObjectConverter = new JsonObjectToObjectConverter(objectMapper);


    @Before
    public void setUp() {
        messageProducerClientPublic = QueueUtil.publicEvents.createProducer();
        publicEventsForSjpPressReportConsumer = QueueUtil.publicEvents.createConsumer(PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED);
        publicEventsForSjpPendingReportConsumer = QueueUtil.publicEvents.createConsumer(PUBLIC_SJP_PENDING_CASES_PUBLIC_LIST_GENERATED);
    }

    @Test
    public void shouldRaisePublishedEventWhenSjpPressListPublicEventConsumed() throws IOException {
        enablePubHubFeature(true);

        final String payload = FileUtil.getPayload("stub-data/public.sjp.press-transparency-report-generated.json");

        sendMessage(messageProducerClientPublic, PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED, stringToJsonObjectConverter.convert(payload), metadataBuilder()
                .withId(randomUUID())
                .withName(PUBLIC_SJP_PRESS_TRANSPARENCY_REPORT_GENERATED)
                .withUserId(randomUUID().toString())
                .build());
        assertNotNull(doVerifySjpPublicEvent(publicEventsForSjpPressReportConsumer) );

        final MessageConsumer consumer = QueueUtil.privateEvents.createConsumer("stagingpubhub.event.sjp-press-published");

        Optional<JsonObject> jsonObject = QueueUtil.retrieveMessageAsJsonObject(consumer);

        final PressTransparencyReportGenerated pressTransparencyReportGenerated = jsonToObjectConverter.convert(jsonObject.get(), PressTransparencyReportGenerated.class);
        assertThat(pressTransparencyReportGenerated.getLanguage().toString(), equalTo("ENGLISH"));
        assertNotNull(pressTransparencyReportGenerated.getListPayload());

        final List<String> expectedDetails = newArrayList("SJP Press list");
        verifyPublicationApi(expectedDetails);
    }

    @Test
    public void shouldRaisePublishedEventWhenSjpPendingListPublicEventConsumed() throws IOException {
        enablePubHubFeature(true);

        final String payload = FileUtil.getPayload("stub-data/public.sjp.pending-cases-public-list-generated.json");

        sendMessage(messageProducerClientPublic, PUBLIC_SJP_PENDING_CASES_PUBLIC_LIST_GENERATED, stringToJsonObjectConverter.convert(payload), metadataBuilder()
                .withId(randomUUID())
                .withName(PUBLIC_SJP_PENDING_CASES_PUBLIC_LIST_GENERATED)
                .withUserId(randomUUID().toString())
                .build());
        assertNotNull(doVerifySjpPublicEvent(publicEventsForSjpPendingReportConsumer) );

        final MessageConsumer consumer = QueueUtil.privateEvents.createConsumer("stagingpubhub.event.sjp-public-published");

        Optional<JsonObject> jsonObject = QueueUtil.retrieveMessageAsJsonObject(consumer);

        final PublicReportGenerated publicReportGenerated = jsonToObjectConverter.convert(jsonObject.get(), PublicReportGenerated.class);
        assertThat(publicReportGenerated.getLanguage().toString(), equalTo("ENGLISH"));
        assertNotNull(publicReportGenerated.getListPayload());

        final List<String> expectedDetails = newArrayList("SJP Pending list");
        verifyPublicationApi(expectedDetails);
    }


    private String doVerifySjpPublicEvent(final MessageConsumer publicEventConsumer) {
        final Optional<JsonObject> message = QueueUtil.retrieveMessageAsJsonObject(publicEventConsumer);
        final JsonObject sjpPublicEvent = message.get();
        return sjpPublicEvent.getJsonObject("listPayload").getString("generatedDateAndTime");
    }

    public  void sendMessage(final MessageProducer messageProducer, final String eventName, final JsonObject payload, final Metadata metadata) {

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
