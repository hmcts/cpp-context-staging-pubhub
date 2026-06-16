package uk.gov.moj.cpp.staging.pubhub.event.transformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.PublishRequested;
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
public class PublishingHubTransformerTest {

    private JsonEnvelope envelope;

    @InjectMocks
    private PublishingHubTransformer publishingHubTransformer;

    @Spy
    ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Test
    public void shouldTransformStandardCourtListData(){
        final JsonObject payload = FileUtil.givenPayload("stub-data/stagingpubhub.event.publish-requested.json");
        final PublishRequested publishRequested = jsonObjectConverter.convert(payload, PublishRequested.class);

        PubhubMaster publishingHubList = publishingHubTransformer.transformStandardList(publishRequested);
        assertThat(publishingHubList.getDocument().getDocumentName(), is("Magistrates Standard List English"));
    }
}
