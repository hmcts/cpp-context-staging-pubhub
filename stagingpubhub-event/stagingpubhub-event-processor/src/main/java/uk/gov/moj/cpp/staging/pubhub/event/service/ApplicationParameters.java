package uk.gov.moj.cpp.staging.pubhub.event.service;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.common.configuration.Value;

import javax.inject.Inject;

public class ApplicationParameters {

    @Inject
    @Value(key = "publishingHub.apim.invocation.retryInterval", defaultValue = "1000")
    public String retryInterval;

    //STE Mock URL - https://spnl-apim-int-gw.cpp.nonlive/publishing-hub/publication
    @Inject
    @Value(key = "publishingHubUrl", defaultValue = "http://localhost:8080/publishing-hub/publication")
    private String publishingHubUrl;

    @Inject
    @Value(key = "publishingHubUrlV2", defaultValue = "https://spnl-apim-int-gw.cpp.nonlive/publishing-hub/v2/publication")
    private String publishingHubUrlV2;


    @Inject
    @Value(key = "publishingHub.subscription.key", defaultValue = "3674a16507104b749a76b29b6c837352")
    private String subscriptionKey;

    @Inject
    @Value(key = "publishingHub.apim.invocation.retryTimes", defaultValue = "3")
    private String retryTimes;

    @Inject
    @GlobalValue(key = "azure.local.mi.clientId", defaultValue = "a9aae3da-14a1-4efe-b4e7-69c07009cd37")
    private String azureLocalMiClientId;

    @Inject
    @GlobalValue(key = "azure.local.mi.tenantId", defaultValue = "e2995d11-9947-4e78-9de6-d44e0603518e")
    private String azureLocalMiTenantId;

    @Inject
    @GlobalValue(key = "azure.dts.fi.clientId", defaultValue = "ff89ffc3-0baa-4651-9b44-d7550e043d65")
    private String azureDtsFiClientId;

    @Inject
    @GlobalValue(key = "azure.dts.fi.tenantId", defaultValue = "531ff96d-0ae9-462a-8d2d-bec7c0b42082")
    private String azureDtsFiTenantId;

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

    public String getAzureLocalMiClientId() {
        return azureLocalMiClientId;
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
}
