package uk.gov.moj.cpp.staging.pubhub.event.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.stagingpubhub.domain.PubhubMaster;
import uk.gov.moj.cpp.staging.pubhub.event.processor.util.FileUtil;

import javax.inject.Inject;
import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

@RunWith(MockitoJUnitRunner.class)
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

}