package uk.gov.moj.cpp.staging.pubhub.event.service;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.common.configuration.Value;

import javax.inject.Inject;

public class ApplicationParameters {

    @Inject
    @Value(key = "publishingHub.apim.invocation.retryInterval", defaultValue = "1000")
    public String retryInterval;

    @Inject
    @Value(key = "publishingHubUrl", defaultValue = "http://localhost:8080/publishing-hub/publication")
    private String publishingHubUrl;

    @Inject
    @Value(key = "publishingHubUrlV2", defaultValue = "https://localhost:8080/publishing-hub/v2/publication") // gitleaks:allow
    private String publishingHubUrlV2;

    @Inject
    @Value(key = "publishingHub.subscription.key", defaultValue = "${PUBLISHING_HUB_SUBSCRIPTION_KEY}")
    private String subscriptionKey;

    @Inject
    @Value(key = "publishingHub.apim.invocation.retryTimes", defaultValue = "3")
    private String retryTimes;

    @Inject
    @GlobalValue(key = "azure.local.mi.apimAuth.clientId") // gitleaks:allow
    private String azureLocalMiApimAuthClientId;

    @Inject
    @GlobalValue(key = "azure.local.mi.tenantId") // gitleaks:allow
    private String azureLocalMiTenantId;

    @Inject
    @GlobalValue(key = "azure.dts.fi.clientId")
    private String azureDtsFiClientId;

    @Inject
    @GlobalValue(key = "azure.dts.fi.tenantId")
    private String azureDtsFiTenantId;

    @Inject
    @GlobalValue(key = "azure.dts.appRegistration.Id")
    private String azureDtsAppRegistrationId;

    @Inject
    @GlobalValue(key = "azure.local.scope", defaultValue = "https://management.azure.com/.default")  // gitleaks:allow
    private String azureLocalScope;

    public String getPublishingHubUrl() {
        return publishingHubUrl;
    }

    public String getPublishingHubUrlV2() {
        return publishingHubUrlV2;
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public String getRetryTimes() {
        return retryTimes;
    }

    public String getRetryInterval() {
        return retryInterval;
    }

    public String getAzureLocalMiApimAuthClientId() {
        return azureLocalMiApimAuthClientId;
    }

    public String getAzureLocalMiTenantId() {
        return azureLocalMiTenantId;
    }

    public String getAzureDtsFiClientId() {
        return azureDtsFiClientId;
    }

    public String getAzureDtsFiTenantId() {
        return azureDtsFiTenantId;
    }

    public String getAzureDtsAppRegistrationId() {
        return azureDtsAppRegistrationId;
    }

    public String getAzureLocalScope() {
        return azureLocalScope;
    }
}
