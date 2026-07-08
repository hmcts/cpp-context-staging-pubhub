package uk.gov.moj.cpp.staging.pubhub;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelopeProvider;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StagingPubHubCommandApiTest {

    @Mock
    private Sender sender;

    @InjectMocks
    private StagingPubHubCommandApi stagingPubHubCommandApi;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeCaptor;

    @Test
    public void isHandler() {
        assertThat(StagingPubHubCommandApi.class, isHandlerClass(COMMAND_API)
                .with(
                        method("publishStandard")
                                .thatHandles("stagingpubhub.command.publish-standard-list")
                )
        );
    }

    @Test
    public void handlePublishStandard() {
        final JsonObject payload = buildStandardListPayload();
        final JsonEnvelope commandEnvelope = createCommandEnvelope(payload, "stagingpubhub.command.publish-standard-list");
        stagingPubHubCommandApi.publishStandard(commandEnvelope);
        verifyResults(payload, "stagingpubhub.command.handler.publish-standard-list");
    }

    private JsonObject buildStandardListPayload() {
        return createObjectBuilder()
                .add("listType", "public")
                .add("courtCentreName", "Lavender Hill Magistrates' Court")
                .add("courtCentreAddress1", "176A Lavender Hill London")
                .add("courtCentreAddress2", "   SW11 1JU")
                .add("welshCourtCentreName", "Llys Ynadon Lavender Hill")
                .add("welshCourtCentreAddress1", " ")
                .add("welshCourtCentreAddress2", "   SW11 1JU")
                .add("hearingDates", createObjectBuilder()
                        .add("hearingDate","2022-04-07")
                        .add("hearingDateWelsh","7 Ebrill 2022")
                        .add("courtRooms",createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("courtRoomName", "Courtroom 01")
                                        .add("welshCourtRoomName", "Courtroom 01")
                                        .add("judiciaryNames", "Miss Atkins JP, Mrs Colman, District Judge (MC) Daber, Mr Hartopp JP, Mrs Maule-Farrell JP, Mr Rahman JP")
                                        .add("welshJudiciaryNames", "Atkins, Mrs  Colman,   Daber, Mr  Hartopp YH, Mrs  Maule-Farrell YH, Mr  Rahman YH")
                                        .add("timeslots", createArrayBuilder().add(
                                                createObjectBuilder().add("hearing",createArrayBuilder()
                                                        .add(createObjectBuilder()
                                                                .add("id","1a0f21ad-d9dc-4e42-9fb3-da2c5de0d7c2")
                                                                .add("sequence","1")
                                                                .add("hearingType","First hearing")
                                                                .build()
                                                        ).build())
                                        ).build()
                                        ).build()
                                ).build()
                        ).build()
                ).build();
    }


    private void verifyResults(final JsonObject payload, final String commandName) {
        verify(sender, atLeastOnce()).send(envelopeCaptor.capture());
        final DefaultEnvelope capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.metadata().name(), is(commandName));
        assertThat(capturedEnvelope.payload(), is(payload));
    }

    private JsonEnvelope createCommandEnvelope(final JsonObject payload, final String commandName) {
        final UUID uuid = randomUUID();
        final UUID userId = randomUUID();

        final Metadata metadata = Envelope
                .metadataBuilder()
                .withName(commandName)
                .withId(uuid)
                .withUserId(userId.toString())
                .build();
        return new DefaultJsonEnvelopeProvider().envelopeFrom(metadata, payload);
    }

}
