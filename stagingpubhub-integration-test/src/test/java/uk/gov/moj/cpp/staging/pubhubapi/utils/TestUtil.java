package uk.gov.moj.cpp.staging.pubhubapi.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils.stubPingFor;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient;

import java.io.IOException;
import java.util.Optional;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.jayway.jsonpath.ReadContext;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matcher;

public class TestUtil {

    public static final String USER_ID = randomUUID().toString();
    public static final String BASE_QUERY = "/usersgroups-service/query/api/rest/usersgroups";
    public static final String RELATEDCASES_PERMISSION = "/users/logged-in-user/permissions";
    public static final String GET_RELATEDCASES_PERMISSION_QUERY = BASE_QUERY + RELATEDCASES_PERMISSION;
    public static final String USERS_GROUPS_SERVICE_NAME = "usergroups-service";

    public static void stubAccessControl(final boolean grantAccess, final String... groupNames) {

        final JsonArrayBuilder groupsArray = createArrayBuilder();

        if (grantAccess) {
            for (final String groupName : groupNames) {
                groupsArray.add(createObjectBuilder()
                        .add("groupId", randomUUID().toString())
                        .add("groupName", groupName)
                );
            }
        }

        final JsonObject response = createObjectBuilder()
                .add("groups", groupsArray).build();

        stubPingFor(USERS_GROUPS_SERVICE_NAME);
        stubFor(get(urlPathMatching("/usersgroups-service/query/api/rest/usersgroups/users/[^/]*/groups"))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, USER_ID)
                        .withHeader(CONTENT_TYPE, "application/json")
                        .withBody(response.toString())));
        WiremockTestHelper.waitForStubToBeReady("/usersgroups-service/query/api/rest/usersgroups/users/" + USER_ID + "/groups", "application/json");

    }


    public static void postCommandAndVerify(final String payload, final String apiName,
                                            final String mediaType) throws IOException {
        final Response response = RestHelper
                .postCommand(AbstractTestHelper.getWriteUrl(apiName), mediaType, payload);
        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));
    }

    public static void postCommandAndVerifyForForbidden(final String payload, final String apiName,
                                            final String mediaType) throws IOException {
        final Response response = RestHelper
                .postCommand(AbstractTestHelper.getWriteUrl(apiName), mediaType, payload);
        assertThat(response.getStatusCode(), equalTo(SC_FORBIDDEN));
    }

    public static void postMessageToTopicAndVerify(final String payload, final String eventName, final String commandName,
                                                   final boolean verify, final Matcher<? super ReadContext>... matchers) {
        final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

        try (final JMSTopicHelper publicTopicHelper = new JMSTopicHelper(); final MessageConsumerClient caseManagement = new MessageConsumerClient()) {
            caseManagement.startConsumer(eventName, "cpscasemanagement.event");
            publicTopicHelper.startProducer("public.event");
            publicTopicHelper.sendMessage(commandName, stringToJsonObjectConverter.convert(payload));
            if (verify) {
                Optional<String> message = caseManagement.retrieveMessage(80000L);
                assertThat(eventName + " message not found in cpscasemanagement.event topic", message.isPresent(), is(true));
                assertThat(message.get(), isJson(allOf(matchers)));
            }
        }
    }


}
