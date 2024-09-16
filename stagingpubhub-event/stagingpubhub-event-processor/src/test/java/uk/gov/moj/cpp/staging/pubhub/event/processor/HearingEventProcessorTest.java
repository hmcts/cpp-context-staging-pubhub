package uk.gov.moj.cpp.staging.pubhub.event.processor;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.featurecontrol.FeatureControlGuard;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.staging.pubhub.event.processor.util.FileUtil;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingEventProcessorTest {
    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> publicEventArgumentCaptor;

    @Mock
    private Sender sender;

    @Mock
    private FeatureControlGuard featureControlGuard;

    @InjectMocks
    private HearingEventProcessor hearingEventProcessor;

    @Test
    public void shouldHandleLiveStatusPublishedPublicEventWhenFeatureIsEnabled() {

        final JsonObject liveStatusPayload = FileUtil.givenPayload("stub-data/public.hearing.live-status-published.json");

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.hearing.live-status-published"),
                liveStatusPayload);

        when(featureControlGuard.isFeatureEnabled("PUBHUB")).thenReturn(true);

        hearingEventProcessor.handleLiveStatusPublishedPublicEvent(event);

        verify(this.sender).send(this.publicEventArgumentCaptor.capture());

        final Envelope<JsonObject> commandEvent = this.publicEventArgumentCaptor.getValue();

        assertThat(commandEvent.metadata().name(), is("stagingpubhub.command.handler.live-status-published"));

        assertThat(commandEvent.payload().toString(), isJson(allOf(
                withJsonPath("$.documentName", equalTo("Live Case Status Update"))
        )));
    }
}
