package uk.gov.moj.cpp.staging.pubhub.event.service;

import uk.gov.moj.cpp.staging.pubhub.exception.AzureAPIMInvocationException;

import javax.inject.Inject;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@SuppressWarnings({"squid:CommentedOutCodeLine", "squid:CallToDeprecatedMethod"})
public class AzureIdentityService {
    public static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    public static final String AZURE_TENANT_ID = "AZURE_TENANT_ID";
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureIdentityService.class);

    @Inject
    private ApplicationParameters applicationParameters;

    public String getTokenFromLocalClientSecretCredentials() {
        String accessToken = null;
        final Configuration configuration = new Configuration();
        configuration.put(AZURE_CLIENT_ID, applicationParameters.getAzureLocalMiApimAuthClientId());
        configuration.put(AZURE_TENANT_ID, applicationParameters.getAzureLocalMiTenantId());

        try {
            final ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
                    .configuration(configuration)
                    .build();
            final TokenRequestContext context = getTokenRequestContext();
            final Mono<String> accessTokenMono = managedIdentityCredential.getToken(context)
                    .map(AccessToken::getToken);
            accessToken = accessTokenMono.block();
        } catch (AzureAPIMInvocationException e) {
            LOGGER.error("Failed to acquire Local Access token", e);
        }
        return accessToken;
    }

    public String getTokenFromRemoteClientSecretCredentials() {
        String accessToken = null;
        final Configuration configuration = new Configuration();
        configuration.put(AZURE_CLIENT_ID, applicationParameters.getAzureDtsFiClientId());
        configuration.put(AZURE_TENANT_ID, applicationParameters.getAzureDtsFiTenantId());

        try {
            final ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
                    .configuration(configuration)
                    .build();
            final TokenRequestContext context = getRemoteTokenRequestContext();
            final Mono<String> accessTokenMono = managedIdentityCredential.getToken(context)
                    .map(AccessToken::getToken);
            accessToken = accessTokenMono.block();
        } catch (AzureAPIMInvocationException e) {
            LOGGER.error("Failed to acquire Remote Access Token", e);
        }
        return accessToken;
    }

    private TokenRequestContext getTokenRequestContext() {
        return new TokenRequestContext()
                .addScopes(applicationParameters.getAzureLocalScope());
    }

    private TokenRequestContext getRemoteTokenRequestContext() {
        return new TokenRequestContext()
                .addScopes(getRemoteScope());
    }

    private String getRemoteScope() {
        return "api://" + applicationParameters.getAzureDtsAppRegistrationId() + "/.default";
    }
}