package uk.gov.moj.cpp.staging.pubhub.event.processor;

import static org.mockito.Matchers.any;
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
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.moj.cpp.staging.pubhub.event.processor.util.FileUtil;
import uk.gov.moj.cpp.staging.pubhub.event.service.PublishingService;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.PublishingHubTransformer;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
    ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    @Mock
    private PublishingService publishingService;

    @Captor
    private ArgumentCaptor<String> publishRequestedArgumentCaptor;

    @Before
    public void setUp() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldSendSuccessMessageWhenPublishRequested() {
        final JsonObject payload = FileUtil.givenPayload("stub-data/stagingpubhub.event.publish-requested.json");
        final PublishRequested publishRequested = jsonObjectConverter.convert(payload, PublishRequested.class);
        final JsonObject publishRequestedJsonObject = this.objectToJsonObjectConverter.convert(publishRequested);
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("stagingpubhub.event.publish-requested"), publishRequestedJsonObject);

        final JsonObject transformPayload = FileUtil.givenPayload("stub-data/publish-requested.json");
        final PubhubMaster publishingHubList = jsonObjectConverter.convert(transformPayload, PubhubMaster.class);

        when(publishingHubTransformer.transformStandardList(any())).thenReturn(publishingHubList);
        when(objectToJsonObjectConverter.convert(publishingHubList)).thenReturn(transformPayload);

        publishEventProcessor.publishRequested(envelope);
        verify(publishingService, times(1)).sendData(publishRequestedArgumentCaptor.capture(), any(), any(), any(), any());
    }
}