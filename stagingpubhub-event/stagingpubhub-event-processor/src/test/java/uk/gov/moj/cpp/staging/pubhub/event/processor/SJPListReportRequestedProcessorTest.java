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
import uk.gov.justice.services.core.featurecontrol.FeatureControlGuard;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.moj.cpp.staging.pubhub.event.processor.util.FileUtil;
import uk.gov.moj.cpp.staging.pubhub.event.service.PublishingService;
import uk.gov.moj.cpp.staging.pubhub.event.transformer.SjpPublishingHubTransformer;

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
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelope;


import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SJPListReportRequestedProcessorTest {

    @Mock
    private Sender sender;

    @Mock
    private SjpPublishingHubTransformer sjpPublishingHubTransformer;

    @InjectMocks
    private SJPListReportRequestedProcessor sjpListReportRequestedProcessor;

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
    private ArgumentCaptor<Envelope> publishRequestedArgumentCaptor;

    private static final String STAGING_PUBHUB_COMMAND_SJP_PRESS = "stagingpubhub.command.handler.sjp-press-published";
    private static final String STAGING_PUBHUB_COMMAND_SJP_PUBLIC = "stagingpubhub.command.handler.sjp-public-published";


    @Mock
    private FeatureControlGuard featureControlGuard;


    @Before
    public void setUp() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void shouldSendSuccessMessageWhenSjpPressReportPublishRequested() {

        when(featureControlGuard.isFeatureEnabled("PUBHUB")).thenReturn(true);

        final JsonObject payload = FileUtil.givenPayload("stub-data/public.sjp.press-transparency-report-generated.json");
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.sjp.press-transparency-report-generated"), payload);

        final JsonObject transformPayload = FileUtil.givenPayload("stub-data/publish-requested-sjp.json");
        final PubhubMaster publishingHubList = jsonObjectConverter.convert(transformPayload, PubhubMaster.class);

        when(sjpPublishingHubTransformer.transformSjpList(any(), any())).thenReturn(publishingHubList);
        when(objectToJsonObjectConverter.convert(publishingHubList)).thenReturn(transformPayload);

        sjpListReportRequestedProcessor.publishSjpTransparencyPressReportRequested(envelope);
        verify(this.sender, times(1)).send(this.publishRequestedArgumentCaptor.capture());
        assertThat(publishRequestedArgumentCaptor.getValue(), notNullValue());
        final List allValues = publishRequestedArgumentCaptor.getAllValues();
        assertThat(allValues.size(), is(1));
        assertThat(((DefaultJsonEnvelope) allValues.get(0)).metadata().name(), equalTo(STAGING_PUBHUB_COMMAND_SJP_PRESS));
        assertThat(((DefaultJsonEnvelope) allValues.get(0)).asJsonObject().getString("language"), equalTo("ENGLISH"));

    }

    @Test
    public void shouldSendSuccessMessageWhenSjpPendingReportPublishRequested() {

        when(featureControlGuard.isFeatureEnabled("PUBHUB")).thenReturn(true);

        final JsonObject payload = FileUtil.givenPayload("stub-data/public.sjp.pending-cases-public-list-generated.json");
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.sjp.pending-cases-public-list-generated"), payload);

        final JsonObject transformPayload = FileUtil.givenPayload("stub-data/publish-requested-sjp.json");
        final PubhubMaster publishingHubList = jsonObjectConverter.convert(transformPayload, PubhubMaster.class);

        when(sjpPublishingHubTransformer.transformSjpList(any(), any())).thenReturn(publishingHubList);
        when(objectToJsonObjectConverter.convert(publishingHubList)).thenReturn(transformPayload);

        sjpListReportRequestedProcessor.publishSjpPublicReportRequested(envelope);
        verify(this.sender, times(1)).send(this.publishRequestedArgumentCaptor.capture());
        assertThat(publishRequestedArgumentCaptor.getValue(), notNullValue());
        final List allValues = publishRequestedArgumentCaptor.getAllValues();
        assertThat(allValues.size(), is(1));
        assertThat(((DefaultJsonEnvelope) allValues.get(0)).metadata().name(), equalTo(STAGING_PUBHUB_COMMAND_SJP_PUBLIC));
        assertThat(((DefaultJsonEnvelope) allValues.get(0)).asJsonObject().getString("language"), equalTo("ENGLISH"));
    }

}