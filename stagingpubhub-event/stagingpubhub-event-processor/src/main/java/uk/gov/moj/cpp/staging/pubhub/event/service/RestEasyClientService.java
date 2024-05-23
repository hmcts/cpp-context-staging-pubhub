package uk.gov.moj.cpp.staging.pubhub.event.service;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.staging.pubhub.json.schema.Meta;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableMap;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2139", "squid:S00112", "squid:S2142"})
public class RestEasyClientService {

    public static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_TOKEN = "Bearer %s";
    public static final String EXTERNAL_SERVICE_ACCESS_TOKEN = "external-service-access-token";
    public static final String OCP_APIM_TRACE = "Ocp-Apim-Trace";
    public static final String TRUE = "true";
    public static final String X_PROVENANCE = "x-provenance";
    public static final String X_TYPE = "x-type";
    public static final String X_LIST_TYPE = "x-list-type";
    public static final String X_COURT_ID = "x-court-id";
    public static final String X_CONTENT_DATE = "x-content-date";
    public static final String X_LANGUAGE = "x-language";
    public static final String X_SENSITIVITY = "x-sensitivity";
    public static final String X_DISPLAY_FROM = "x-display-from";
    public static final String X_DISPLAY_TO = "x-display-to";

    @Inject
    @Value(key = "restEasyClientConnectionPoolSize", defaultValue = "10")
    private String restEasyClientConnectionPoolSize;

    ResteasyClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestEasyClientService.class);

    @PostConstruct
    public void createClient() {
        client = new ResteasyClientBuilder().disableTrustManager()
                .connectionPoolSize(Integer.parseInt(restEasyClientConnectionPoolSize))
                .build();
    }

    public Response post(final String url, final String payload, final String key) {
        final Invocation.Builder request = this.client.target(url).request();
        request.headers(new MultivaluedHashMap(getHeaders(key)));
        return request.post(Entity.json(payload));
    }

    public Response post(final String url, final String payload, final String key, final Meta meta) {
        final Invocation.Builder request = this.client.target(url).request();
        request.headers(new MultivaluedHashMap(getHeaders(key, meta)));
        return request.post(Entity.json(payload));
    }

    public Response post(final String url, final String payload, final String localServiceAccessToken, final String remoteServiceAccessToken, final Meta meta) {
        LOGGER.info("local token : {} and remote token : {}",localServiceAccessToken,remoteServiceAccessToken);
        final Invocation.Builder request = this.client.target(url).request();
        request.headers(new MultivaluedHashMap(getHeaders(localServiceAccessToken, remoteServiceAccessToken, meta)));
        return request.post(Entity.json(payload));
    }

    private Map<String, String> getHeaders(final String subscriptionKey) {
        return ImmutableMap.of(
                ACCEPT, MediaType.APPLICATION_JSON,
                OCP_APIM_SUBSCRIPTION_KEY, subscriptionKey,
                OCP_APIM_TRACE, TRUE);
    }

    private Map<String, String> getHeaders(final String subscriptionKey, final Meta meta) {
        final ImmutableMap map = ImmutableMap.builder()
                .put(ACCEPT,MediaType.APPLICATION_JSON)
                .put(OCP_APIM_SUBSCRIPTION_KEY, subscriptionKey)
                .put(OCP_APIM_TRACE, TRUE)
                .put(X_PROVENANCE, meta.getProvenance())
                .put(X_TYPE, meta.getType())
                .put(X_LIST_TYPE, meta.getListType())
                .put(X_COURT_ID,meta.getCourtId())
                .put(X_CONTENT_DATE,meta.getContentDate())
                .put(X_LANGUAGE, meta.getLanguage())
                .put(X_SENSITIVITY, meta.getSensitivity())
                .put(X_DISPLAY_FROM, meta.getDisplayFrom())
                .put(X_DISPLAY_TO,meta.getDisplayTo())
                .build();
        return ImmutableMap.copyOf(map);
    }

    private Map<String, String> getHeaders(final String localServiceAccessToken, final String remoteServiceAccessToken, final Meta meta) {
        final ImmutableMap map = ImmutableMap.builder()
                .put(AUTHORIZATION, String.format(BEARER_TOKEN, localServiceAccessToken))
                .put(ACCEPT, MediaType.APPLICATION_JSON)
                .put(EXTERNAL_SERVICE_ACCESS_TOKEN, remoteServiceAccessToken)
                .put(OCP_APIM_TRACE, TRUE)
                .put(X_PROVENANCE, meta.getProvenance())
                .put(X_TYPE, meta.getType())
                .put(X_LIST_TYPE, meta.getListType())
                .put(X_COURT_ID,meta.getCourtId())
                .put(X_CONTENT_DATE,meta.getContentDate())
                .put(X_LANGUAGE, meta.getLanguage())
                .put(X_SENSITIVITY, meta.getSensitivity())
                .put(X_DISPLAY_FROM, meta.getDisplayFrom())
                .put(X_DISPLAY_TO,meta.getDisplayTo())
                .build();
        return ImmutableMap.copyOf(map);
    }
}
