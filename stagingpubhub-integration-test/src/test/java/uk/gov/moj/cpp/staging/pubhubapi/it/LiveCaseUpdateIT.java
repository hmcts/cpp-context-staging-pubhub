package uk.gov.moj.cpp.staging.pubhubapi.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.listAllStubMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.FeatureToggleUtil.enablePubHubFeature;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.QueueUtil.sendMessage;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.justice.staging.pubhub.json.schema.PublishLiveStatus;
import uk.gov.moj.cpp.staging.pubhubapi.utils.FileUtil;
import uk.gov.moj.cpp.staging.pubhubapi.utils.QueueUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.json.JsonObject;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

@SuppressWarnings({"squid:S1607"})
public class LiveCaseUpdateIT {
    private static final String PUBLIC_HEARING_LIVE_CASE_UPDATE="public.hearing.live-status-published";
    private static MessageProducer messageProducerClientPublic = publicEvents.createPublicProducer();

    private static final String userId = UUID.randomUUID().toString();

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @AfterAll
    public static void tearDownClass() throws JMSException {
        messageProducerClientPublic.close();
    }

    @Test
    @Disabled("DD-33038: Multiple copies of courtRoom and other schema classes with different attributes are causing Json to Object conversion error at random." +
            "This functionality is feature toggled and not live in production")
    public void shouldProcessLiveCaseUpdatePublicEvent(){
        enablePubHubFeature(true);
        final JsonObject liveStatusPayload = FileUtil.givenPayload("stub-data/public.hearing.live-status-published.json");
        final MessageConsumer consumer = QueueUtil.privateEvents.createConsumer("stagingpubhub.event.publish-live-status");

        final String payload = FileUtil.getPayload("stub-data/live-case-status.json");
        stubFor(post(urlPathEqualTo("/publishing-hub/publication"))
                .withRequestBody(equalToJson(payload))
                .withHeader("x-type",containing("LCSU"))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("x-type","LCSU")
                        .withHeader("Ocp-Apim-Subscription-Key", "3674a16507104b749a76b29b6c837352")
                        .withHeader("Ocp-Apim-Trace", "true")));

        //Send public event
        sendMessage(messageProducerClientPublic,
                PUBLIC_HEARING_LIVE_CASE_UPDATE, liveStatusPayload, JsonEnvelope.metadataBuilder()
                        .withId(randomUUID())
                        .withName(PUBLIC_HEARING_LIVE_CASE_UPDATE)
                        .withUserId(userId)
                        .build());

        Optional<JsonObject> jsonObject = QueueUtil.retrieveMessageAsJsonObject(consumer);
        final PublishLiveStatus publishLiveStatus = jsonObjectConverter.convert(jsonObject.get(), PublishLiveStatus.class);

        assertThat(publishLiveStatus.getDocumentName(),equalTo("Live Case Updates"));

        final AtomicReference<String> body = new AtomicReference<>();
        List<StubMapping> mappings =  listAllStubMappings().getMappings();
        mappings.forEach(stubMapping -> {
            String json = nonNull(stubMapping.getRequest().getBodyPatterns())? stubMapping.getRequest().getBodyPatterns().get(0).toString():"";
                    if(json.contains("Live Case Updates")){
                        body.set(json);
                    }
        });

        assertThat(body.get().contains("Live Case Updates"),equalTo(true));
    }

    @Test
    @Disabled
    public void shouldProcessLiveCaseUpdatePublicEventWhenFeatureToggleOff(){
        enablePubHubFeature(false);
        final JsonObject liveStatusPayload = FileUtil.givenPayload("stub-data/public.hearing.live-status-published.json");
        final MessageConsumer consumer = QueueUtil.privateEvents.createConsumer("stagingpubhub.event.publish-live-status");

        //Send public event
        sendMessage(messageProducerClientPublic,
                PUBLIC_HEARING_LIVE_CASE_UPDATE, liveStatusPayload, JsonEnvelope.metadataBuilder()
                        .withId(randomUUID())
                        .withName(PUBLIC_HEARING_LIVE_CASE_UPDATE)
                        .withUserId(userId)
                        .build());
        Optional<JsonObject> jsonObject = QueueUtil.retrieveMessageAsJsonObject(consumer);


        assertThat(jsonObject.isPresent(), equalTo(false));
    }
}
