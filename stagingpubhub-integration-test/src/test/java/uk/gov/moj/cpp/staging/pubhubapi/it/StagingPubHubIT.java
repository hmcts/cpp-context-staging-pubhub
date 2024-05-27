package uk.gov.moj.cpp.staging.pubhubapi.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.staging.pubhubapi.stub.PublishingServiceStub.verifyPublicationApi;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.FeatureToggleUtil.enablePubHubFeature;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.TestUtil.postCommandAndVerify;
import static uk.gov.moj.cpp.staging.pubhubapi.utils.TestUtil.postCommandAndVerifyForForbidden;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.moj.cpp.staging.pubhubapi.utils.FileUtil;
import uk.gov.moj.cpp.staging.pubhubapi.utils.QueueUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;


public class StagingPubHubIT {
    private static final String STANDARD = "Standard";
    private MessageConsumer consumer = QueueUtil.privateEvents.createConsumer("stagingpubhub.event.publish-requested");

    @Spy
    ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectToObjectConverter(objectMapper);


    @Ignore
    @Test
    public void shouldRaiseStandardListPublishedEventWhenPubHubEnabled() throws IOException {
        enablePubHubFeature(true);

        final String payload = FileUtil.getPayload("stub-data/stagingpubhub.command.publish-standard-list.json");
        stubFor(post(urlPathEqualTo("/publishing-hub/publication"))
                .withRequestBody(equalToJson(payload))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("Ocp-Apim-Subscription-Key", "3674a16507104b749a76b29b6c837352")
                        .withHeader("Ocp-Apim-Trace", "true")));

        postCommandAndVerify(
                payload,
                "/pubhub",
                "application/vnd.publish-standard-list+json");
        Optional<JsonObject> jsonObject = QueueUtil.retrieveMessageAsJsonObject(consumer);

        final PublishRequested publishRequested = jsonObjectConverter.convert(jsonObject.get(), PublishRequested.class);
        assertThat(publishRequested.getListType(), equalTo(STANDARD));

        final List<String> expectedDetails = newArrayList("Magistrates Standard List English");
        verifyPublicationApi(expectedDetails);
    }

    @Test
    public void shouldGetForbiddenWhenCallStandardListEventWhenPubHubDisabled() throws IOException {
        enablePubHubFeature(false);
        final String payload = FileUtil.getPayload("stub-data/stagingpubhub.command.publish-standard-list.json");

        postCommandAndVerifyForForbidden(
                payload,
                "/pubhub",
                "application/vnd.publish-standard-list+json");
    }

    @After
    public void tearDown() throws JMSException {
        consumer.close();
    }
}
