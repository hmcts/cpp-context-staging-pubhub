package uk.gov.moj.cpp.staging.pubhub.event.transformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.moj.cpp.staging.pubhub.event.processor.util.FileUtil;

import javax.inject.Inject;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SjpPublishingHubTransformerTest {


    private JsonEnvelope envelope;

    @InjectMocks
    private SjpPublishingHubTransformer sjpPublishingHubTransformer;

    @Spy
    ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Test
    public void shouldTransformSjpPressList(){
        final JsonObject payload = FileUtil.givenPayload("stub-data/public.sjp.press-transparency-report-generated.json");
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.sjp.press-transparency-report-generated"), payload);
        PubhubMaster publishingHubList = sjpPublishingHubTransformer.transformSjpList(envelope, DocumentType.SJP_PRESS_LIST);
        assertThat(publishingHubList.getDocument().getDocumentName(), is(DocumentType.SJP_PRESS_LIST.getValue()));
    }

    @Test
    public void shouldTransformSjpPublicList(){
        final JsonObject payload = FileUtil.givenPayload("stub-data/public.sjp.pending-cases-public-list-generated.json");
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.sjp.pending-cases-public-list-generated"), payload);
        PubhubMaster publishingHubList = sjpPublishingHubTransformer.transformSjpList(envelope, DocumentType.SJP_PUBLIC_LIST);
        assertThat(publishingHubList.getDocument().getDocumentName(), is(DocumentType.SJP_PUBLIC_LIST.getValue()));
    }

}