package uk.gov.moj.cpp.staging.pubhubapi.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.text.MessageFormat.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils.stubPingFor;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;

import java.util.UUID;

import javax.ws.rs.core.Response.Status;

public class WireMockStubUtils {
    private static final String CONTENT_TYPE_QUERY_GROUPS = "application/vnd.usersgroups.groups+json";
    private static final String CONTENT_TYPE_QUERY_REFERENCEDATAOFFENCES = "application/vnd.referencedataoffences.query.offence-poc+json";

    public static void setupAsAuthorisedUser(final UUID userId) {
        stubPingFor("usersgroups-service");

        stubFor(get(urlPathEqualTo(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(FileUtil.getPayload("stub-data/usersgroups.get-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId), CONTENT_TYPE_QUERY_GROUPS, Status.OK);
    }

    public static void setupAsSystemUser(final UUID userId) {
        stubPingFor("usersgroups-service");

        stubFor(get(urlPathEqualTo(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(FileUtil.getPayload("stub-data/usersgroups.get-systemuser-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId), CONTENT_TYPE_QUERY_GROUPS, Status.OK);
    }

    public static void stubReferenceDataOffencesGetOffencePoc(final String cjsOffenceCode) {
        stubPingFor("referencedataoffences-service");

        stubFor(get(urlPathEqualTo(format("/referencedataoffences-service/query/api/rest/referencedataoffences/offence-poc?cjsOffenceCode={0}", cjsOffenceCode)))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(FileUtil.getPayload("stub-data/referencedataoffences.get-offence-poc.json"))));

        waitForStubToBeReady(format("/referencedataoffences-service/query/api/rest/referencedataoffences/offence-poc?cjsOffenceCode={0}", cjsOffenceCode), CONTENT_TYPE_QUERY_REFERENCEDATAOFFENCES, Status.OK);
    }

    public static void stubReferenceDataOffencesGetServiceNotAvailable() {
        stubPingFor("referencedataoffences-service");

        stubFor(get(urlPathEqualTo(format("/referencedataoffences-service/query/api/rest/referencedataoffences/offence-poc?cjsOffenceCode=404")))
                .willReturn(aResponse().withStatus(SC_NOT_FOUND)
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)));

        waitForStubToBeReady(format("/referencedataoffences-service/query/api/rest/referencedataoffences/offence-poc?cjsOffenceCode=404"), CONTENT_TYPE_QUERY_REFERENCEDATAOFFENCES, Status.NOT_FOUND);
    }

    private static void waitForStubToBeReady(final String resource, final String mediaType, final Status expectedStatus) {
        poll(requestParams(format("{0}/{1}", getBaseUri(), resource), mediaType).build())
                .until(status().is(expectedStatus));
    }

}
