package uk.gov.moj.cpp.staging.pubhub.helper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.staging.pubhub.helper.RetryHelper.retryHelper;

import uk.gov.moj.cpp.staging.pubhub.event.transformer.DocumentType;
import uk.gov.moj.cpp.staging.pubhub.exception.AzureAPIMInvocationException;

import java.util.function.IntSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RetryHelperTest {

    @Mock
    IntSupplier supplier;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldInvokeSupplierMethodOnlyOnce() throws Exception {

        when(supplier.getAsInt()).thenReturn(420);

        RetryHelper.Builder builder = retryHelper()
                .withSupplier(() -> supplier.getAsInt())
                .withRetryTimes(3)
                .withRetryInterval(200)
                .withPredicate(statusCode -> statusCode > 429);

        RetryHelper retryHelper = builder.build();
        retryHelper.postWithRetry();

        verify(supplier).getAsInt();
    }

    @Test
    public void shouldThrowExceptionAfterExceedingRetryCount() throws Exception {

        when(supplier.getAsInt()).thenReturn(500);

        final RetryHelper retryHelper = retryHelper()
                .withSupplier(() -> supplier.getAsInt())
                .withRetryTimes(3)
                .withRetryInterval(200)
                .withExceptionSupplier(() -> new AzureAPIMInvocationException(DocumentType.SJP_PRESS_LIST.getValue(), "url"))
                .withPredicate(statusCode -> statusCode > 429)
                .build();

        assertThrows(AzureAPIMInvocationException.class, () -> retryHelper.postWithRetry());
        verify(supplier, times(3)).getAsInt();
    }

}
