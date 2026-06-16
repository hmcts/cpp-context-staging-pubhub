package uk.gov.moj.cpp.staging.pubhub.event.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.moj.cpp.staging.pubhub.event.processor.util.FileUtil;
import uk.gov.moj.cpp.staging.pubhub.event.service.PublishingService;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.PublishingHubTransformer;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishEventProcessorTest {

    @Mock
    private Sender sender;

    @Mock
    private PublishingHubTransformer publishingHubTransformer;

    @InjectMocks
    private PublishEventProcessor publishEventProcessor;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Mock
    private PublishingService publishingService;

    @Captor
    private ArgumentCaptor<String> publishRequestedArgumentCaptor;

    @BeforeEach
    public void setUp() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldSendSuccessMessageWhenPublishRequested() {
        final JsonObject payload = FileUtil.givenPayload("stub-data/stagingpubhub.event.publish-requested.json");
        final PublishRequested publishRequested = jsonObjectToObjectConverter.convert(payload, PublishRequested.class);
        final JsonObject publishRequestedJsonObject = this.objectToJsonObjectConverter.convert(publishRequested);
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("stagingpubhub.event.publish-requested"), publishRequestedJsonObject);

        final JsonObject transformPayload = FileUtil.givenPayload("stub-data/publish-requested.json");
        final PubhubMaster publishingHubList = jsonObjectToObjectConverter.convert(transformPayload, PubhubMaster.class);

        when(publishingHubTransformer.transformStandardList(any())).thenReturn(publishingHubList);
        when(objectToJsonObjectConverter.convert(publishingHubList)).thenReturn(transformPayload);

        publishEventProcessor.publishRequested(envelope);
        verify(publishingService, times(1)).sendData(publishRequestedArgumentCaptor.capture(), any(), any(), any(), any());
    }
}