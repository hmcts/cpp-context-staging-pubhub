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
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.moj.cpp.staging.pubhubapi.utils.FileUtil;
import uk.gov.moj.cpp.staging.pubhubapi.utils.QueueUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.json.JsonObject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

@SuppressWarnings({"squid:S1607"})
public class StagingPubHubIT {
    private static final String STANDARD = "Standard";
    private MessageConsumer consumer = QueueUtil.privateEvents.createConsumer("jms.topic.stagingpubhub.event.publish-requested");

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();


    @Test
    @Disabled("DD-33039: Multiple copies of courtRooms and other schema classes with different attributes are causing Json to Object conversion error at random." +
            "This functionality is feature toggled and not live in production")
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
    @Disabled
    public void shouldGetForbiddenWhenCallStandardListEventWhenPubHubDisabled() throws IOException {
        enablePubHubFeature(false);
        final String payload = FileUtil.getPayload("stub-data/stagingpubhub.command.publish-standard-list.json");

        postCommandAndVerifyForForbidden(
                payload,
                "/pubhub",
                "application/vnd.publish-standard-list+json");
    }

    @AfterEach
    public void tearDown() throws JMSException {
        consumer.close();
    }
}
