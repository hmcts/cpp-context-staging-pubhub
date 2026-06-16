package uk.gov.moj.cpp.staging.pubhubapi.utils;


import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static org.hamcrest.Matchers.containsString;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;

import uk.gov.justice.services.test.utils.core.http.RequestParams;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WiremockTestHelper {

    public static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
    public static final String BASE_URI = "http://" + HOST + ":8080";
    private static final Logger LOGGER = LoggerFactory.getLogger(WiremockTestHelper.class);
    private static final String WIREMOCK_PORT = System.getProperty("WIREMOCK_PORT", "8080");
    private static final String WIREMOCK_BASE_URI = "http://" + HOST + ":" + WIREMOCK_PORT;
    private static final String WIREMOCK_COUNT_URI = WIREMOCK_BASE_URI + "/__admin/requests/count";

    private static final RestClient restClient = new RestClient();

    public static void resetService() {
        configureFor(HOST, 8080);
        reset();
    }

    public static void waitForStubToBeReady(String resource, String mediaType) {
        waitForStubToBeReady(resource, mediaType, Status.OK);
    }

    public static void waitForStubToBeReady(String resource, String mediaType, Status expectedStatus) {
        RequestParams requestParams = requestParams(BASE_URI + resource, mediaType).build();

        poll(requestParams)
                .until(
                        status().is(expectedStatus)
                );
    }

    public static void waitForStubToBeReady(String resource, String mediaType, String expectedInBody) {
        RequestParams requestParams = requestParams(BASE_URI + resource, mediaType).build();

        poll(requestParams)
                .until(
                        status().is(Status.OK),
                        payload().that(containsString(expectedInBody))
                );
    }

    public static void waitForStubToBeReady(String resource, String mediaType, Status expectedStatus, String headerName, String headerValue) {
        RequestParams requestParams = requestParams(BASE_URI + resource, mediaType)
                .withHeader(headerName, headerValue)
                .build();
        poll(requestParams)
                .until(
                        status().is(expectedStatus)
                );
    }


}
