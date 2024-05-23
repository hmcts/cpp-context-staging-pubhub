package uk.gov.moj.cpp.staging.pubhubapi.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;

import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;

public class PublishingServiceStub {
    public static void verifyPublicationApi(final List<String> expectedValues) {
        await().atMost(20, SECONDS).pollInterval(5, SECONDS).until(() -> {
            final RequestPatternBuilder requestPatternBuilder = postRequestedFor(urlPathMatching("/publishing-hub/publication"));
            expectedValues.forEach(
                    expectedValue -> requestPatternBuilder.withRequestBody(containing(expectedValue))
            );
            verify(requestPatternBuilder);
        });
    }

    public static void verifyPublicationV2Api(final List<String> expectedValues) {
        await().atMost(20, SECONDS).pollInterval(5, SECONDS).until(() -> {
            final RequestPatternBuilder requestPatternBuilder = postRequestedFor(urlPathMatching("/publishing-hub/V2/publication"));
            expectedValues.forEach(
                    expectedValue -> requestPatternBuilder.withRequestBody(containing(expectedValue))
            );
            verify(requestPatternBuilder);
        });
    }
}
