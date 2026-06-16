package uk.gov.moj.cpp.staging.pubhubapi.utils;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.UUID;

import javax.jms.MessageConsumer;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractTestHelper implements AutoCloseable {
    public static final String USER_ID = randomUUID().toString();

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestHelper.class);

    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
    protected static final String BASE_URI = System.getProperty("baseUri", "http://" + HOST + ":8080");
    private static final String WRITE_BASE_URL = "/stagingpubhub-service/command/api/rest/stagingpubhub";
    private static final String READ_BASE_URL = "/stagingpubhub-service/query/api/rest/stagingpubhub";

    protected final RestClient restClient = new RestClient();

    protected final MessageConsumerClient publicConsumer = new MessageConsumerClient();
    protected MessageConsumer privateEventsConsumer;
    protected MessageConsumer publicEventsConsumer;


    public static String getWriteUrl(final String resource) {
        return Joiner.on("").join(BASE_URI, WRITE_BASE_URL, resource);
    }

    public static String getReadUrl(final String resource) {
        return Joiner.on("").join(BASE_URI, READ_BASE_URL, resource);
    }

    static {
        doAllStubbing();
    }

    public static void doAllStubbing() {
    }

    public void makePostCall(String url, String mediaType, String payload) {
        makePostCall(fromString(USER_ID), url, mediaType, payload);
    }

    protected void makePostCall(UUID userId, String url, String mediaType, String payload) {
        LOGGER.info("Post call made: \n\n\tURL = {} \n\tMedia type = {} \n\tPayload = {}\n\n", url, mediaType, payload, USER_ID);
        MultivaluedMap<String, Object> map = new MultivaluedHashMap<>();
        map.add(HeaderConstants.USER_ID, userId.toString());
        Response response = restClient.postCommand(url, mediaType, payload, map);
        assertThat(response.getStatus(), is(Response.Status.ACCEPTED.getStatusCode()));
    }

    @Override
    public void close() {
        publicConsumer.close();
        try {
            privateEventsConsumer.close();
            publicEventsConsumer.close();
        } catch (Exception e) {
            LOGGER.error("Error while closing consumer");
        }
    }
}
