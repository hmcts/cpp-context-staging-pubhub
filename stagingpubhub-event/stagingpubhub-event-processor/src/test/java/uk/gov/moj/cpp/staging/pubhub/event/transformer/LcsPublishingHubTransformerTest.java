package uk.gov.moj.cpp.staging.pubhub.event.transformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.staging.pubhub.json.schema.PublishLiveStatus;
import uk.gov.justice.staging.pubhub.schema.PubhubMaster;
import uk.gov.moj.cpp.staging.pubhub.event.processor.util.FileUtil;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LcsPublishingHubTransformerTest {
    private JsonEnvelope envelope;

    @InjectMocks
    private LcsPublishingHubTransformer lcsPublishingHubTransformer;

    @Spy
    ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    @Test
    public void shouldTransformLiveCaseStatus() {
        final JsonObject payload = FileUtil.givenPayload("stub-data/stagingpubhub.event.publish-live-status.json");
        final PublishLiveStatus publishLiveStatus = jsonObjectConverter.convert(payload, PublishLiveStatus.class);

        PubhubMaster publishingHubList = lcsPublishingHubTransformer.transformLcsu(publishLiveStatus);

        JSONObject object = new JSONObject(publishingHubList);
        String json = object.toString();
        System.out.println(json);

        assertThat(publishingHubList.getDocument().getDocumentName(), is("Live Case Updates"));
    }
}
