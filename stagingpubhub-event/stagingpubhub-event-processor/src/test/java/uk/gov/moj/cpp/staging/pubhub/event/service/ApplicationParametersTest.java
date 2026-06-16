package uk.gov.moj.cpp.staging.pubhub.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplicationParametersTest {

    private ApplicationParameters applicationParameters;

    @BeforeEach
    public void setUp() {
        applicationParameters = new ApplicationParameters();
    }

    @Test
    public void shouldReturnRetryInterval() {
        applicationParameters.retryInterval = "2000";
        assertEquals("2000", applicationParameters.getRetryInterval());
    }

    @Test
    public void shouldReturnPublishingHubUrl() throws Exception {
        setField("publishingHubUrl", "http://test-host/publishing-hub");
        assertEquals("http://test-host/publishing-hub", applicationParameters.getPublishingHubUrl());
    }

    @Test
    public void shouldReturnPublishingHubUrlV2() throws Exception {
        setField("publishingHubUrlV2", "https://test-host/publishing-hub/v2");
        assertEquals("https://test-host/publishing-hub/v2", applicationParameters.getPublishingHubUrlV2());
    }

    @Test
    public void shouldReturnSubscriptionKey() throws Exception {
        setField("subscriptionKey", "test-subscription-key");
        assertEquals("test-subscription-key", applicationParameters.getSubscriptionKey());
    }

    @Test
    public void shouldReturnRetryTimes() throws Exception {
        setField("retryTimes", "5");
        assertEquals("5", applicationParameters.getRetryTimes());
    }

    @Test
    public void shouldReturnAzureLocalMiApimAuthClientId() throws Exception {
        setField("azureLocalMiApimAuthClientId", "mi-apim-client-id");
        assertEquals("mi-apim-client-id", applicationParameters.getAzureLocalMiApimAuthClientId());
    }

    @Test
    public void shouldReturnAzureLocalMiTenantId() throws Exception {
        setField("azureLocalMiTenantId", "mi-tenant-id");
        assertEquals("mi-tenant-id", applicationParameters.getAzureLocalMiTenantId());
    }

    @Test
    public void shouldReturnAzureDtsFiClientId() throws Exception {
        setField("azureDtsFiClientId", "dts-fi-client-id");
        assertEquals("dts-fi-client-id", applicationParameters.getAzureDtsFiClientId());
    }

    @Test
    public void shouldReturnAzureDtsFiTenantId() throws Exception {
        setField("azureDtsFiTenantId", "dts-fi-tenant-id");
        assertEquals("dts-fi-tenant-id", applicationParameters.getAzureDtsFiTenantId());
    }

    @Test
    public void shouldReturnAzureDtsAppRegistrationId() throws Exception {
        setField("azureDtsAppRegistrationId", "dts-app-registration-id");
        assertEquals("dts-app-registration-id", applicationParameters.getAzureDtsAppRegistrationId());
    }

    @Test
    public void shouldReturnAzureLocalScope() throws Exception {
        setField("azureLocalScope", "https://management.azure.com/.default");
        assertEquals("https://management.azure.com/.default", applicationParameters.getAzureLocalScope());
    }

    private void setField(final String fieldName, final String value) throws Exception {
        final Field field = ApplicationParameters.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(applicationParameters, value);
    }
}
