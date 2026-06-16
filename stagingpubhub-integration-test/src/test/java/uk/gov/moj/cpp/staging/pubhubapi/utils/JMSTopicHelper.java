package uk.gov.moj.cpp.staging.pubhubapi.utils;


import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.queueUri;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import io.restassured.path.json.JsonPath;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matcher;

public class JMSTopicHelper implements AutoCloseable {

    private static final String USER_ID = randomUUID().toString();
    private static final String QUEUE_URI = queueUri();

    private Session session;
    private MessageProducer messageProducer;
    private Connection connection;

    public static void postMessageToTopicAndVerify(final String payload, final String publicEventName, final String expectedEventToBePublished, final long timeout) {
        postMessageToTopicAndVerify(payload, publicEventName, expectedEventToBePublished, timeout, false);
    }

    public static void postMessageToTopicAndVerify(final String payload, final String publicEventName, final String expectedEventToBePublished, final long timeout, final boolean isExpectingPublicEvent) {
        final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();
        String topicName = "cpscasefile.event";
        if(isExpectingPublicEvent){
            topicName = "jms.topic.public.event";
        }
        try (JMSTopicHelper publicTopicHelper = new JMSTopicHelper(); MessageConsumerClient messageConsumerClient = new MessageConsumerClient()) {
            messageConsumerClient.startConsumer(expectedEventToBePublished, topicName);
            publicTopicHelper.startProducer("jms.topic.public.event");
            publicTopicHelper.sendMessage(publicEventName, stringToJsonObjectConverter.convert(payload), empty());
            assertThat(expectedEventToBePublished + " message not found in "+topicName+" topic", messageConsumerClient.retrieveMessage(timeout).isPresent(), is(true));
        }
    }

    public static void postMessageToTopic(final String sourceFile, final String publicEventName, final String expectedEventName, final UUID caseId, final String urn) throws IOException {
        String payload = readFileToString(new File(JMSTopicHelper.class.getClassLoader().getResource(sourceFile).getFile()));
        payload = payload.replace("TWIF_MESSAGE_ID", randomUUID().toString())
                .replace("CASE_ID", caseId.toString())
                .replace("PTI_URN", urn);
        JMSTopicHelper.postMessageToTopicAndVerify(payload, publicEventName, expectedEventName, 10000L);
    }

    public static String retrieveMessageWithMatchers(final MessageConsumerClient consumer, final Matcher matchers) {

        final AtomicReference<String> message = new AtomicReference<>();
        await().timeout(35, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .pollDelay(500, TimeUnit.MILLISECONDS)
                .until(
                        () -> {
                            Optional<String> textMessage = consumer.retrieveMessage();
                            if (textMessage.isPresent()) {
                                JsonPath jsonPath = new JsonPath(textMessage.get());
                                message.set(jsonPath.prettify());
                            }
                            return message.get();
                        }, (allOf(matchers)));

        return message.get();

    }

    public void startProducer(final String topicName) {

        try {
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(QUEUE_URI);
            connection = factory.createConnection();
            connection.start();

            session = connection.createSession(false, AUTO_ACKNOWLEDGE);
            final Destination destination = session.createTopic(topicName);
            messageProducer = session.createProducer(destination);
        } catch (final JMSException e) {
            close();
            throw new RuntimeException(format("Failed to create message producer to topic: '%s', queue uri: '%s'", topicName, QUEUE_URI), e);
        }
    }

    public void sendMessage(final String commandName, final JsonObject payload) {

        if (messageProducer == null) {
            close();
            throw new RuntimeException("Message producer not started. Please call startProducer(...) first.");
        }

        final Metadata metadata = createMetadataForCommandWith(commandName);

        sendMessage(commandName, payload, metadata);
    }

    public void sendEventMessage(final String commandName,
                                 final JsonObject payload,
                                 final UUID streamId,
                                 final long eventNumber) {

        if (messageProducer == null) {
            close();
            throw new RuntimeException("Message producer not started. Please call startProducer(...) first.");
        }

        final Metadata metadata = createMetadataForEventWith(commandName, streamId, eventNumber);

        sendMessage(commandName, payload, metadata);
    }

    public void sendMessage(final String commandName, final JsonObject payload, Optional<UUID> streamId) {

        if (messageProducer == null) {
            close();
            throw new RuntimeException("Message producer not started. Please call startProducer(...) first.");
        }


        final Metadata metadata = getMetadataWithName(commandName, streamId);

        sendMessage(commandName, payload, metadata);
    }

    private Metadata createMetadataForCommandWith(final String commandName) {

        return Envelope.metadataBuilder()
                .withId(randomUUID())
                .withName(commandName)
                .createdAt(ZonedDateTime.now())
                .withUserId(USER_ID)
                .withClientCorrelationId(randomUUID().toString())
                .withSource(RandomStringUtils.randomAlphanumeric(10))
                .build();
    }
    private Metadata getMetadataWithName(String commandName, Optional<UUID> streamId) {
        MetadataBuilder metadataBuilder = Envelope.metadataBuilder().withId(UUID.randomUUID())
                .withName(commandName)
                .createdAt(ZonedDateTime.now())
                .withUserId(USER_ID)
                .withClientCorrelationId(UUID.randomUUID().toString());

        if (streamId.isPresent()) {
            metadataBuilder = metadataBuilder.withStreamId(streamId.get())
                    .withPosition(1);
        }

        return metadataBuilder.build();
    }

    private Metadata createMetadataForEventWith(final String commandName, final UUID streamId, final long eventNumber) {

        return Envelope.metadataBuilder()
                .withId(randomUUID())
                .withName(commandName)
                .createdAt(ZonedDateTime.now())
                .withUserId(USER_ID)
                .withClientCorrelationId(randomUUID().toString())
                .withSource(RandomStringUtils.randomAlphanumeric(10))
                .withStreamId(streamId)
                .withPosition(1)
                // TODO: This needs fixing. we should not be generating the private event manually through tests (instead trigger the behaviour by dropping a public event or issuing a REST command
                .withPreviousEventNumber(eventNumber - 1)
                .withEventNumber(eventNumber)
                .build();
    }

    private void sendMessage(final String commandName, final JsonObject payload, final Metadata metadata) {

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, payload);
        final String json = jsonEnvelope.toDebugStringPrettyPrint();

        try {
            final TextMessage message = session.createTextMessage();

            message.setText(json);
            message.setStringProperty("CPPNAME", commandName);

            messageProducer.send(message);
        } catch (JMSException e) {
            close();
            throw new RuntimeException("Failed to send message. commandName: '" + commandName + "', json: " + json, e);
        }
    }


    @Override
    public void close() {

        try {
            if (messageProducer != null) {
                messageProducer.close();
            }
        } catch (final JMSException ignored) {
        }
        try {
            if (session != null) {
                session.close();
            }
        } catch (final JMSException ignored) {
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (final JMSException ignored) {
        }

        session = null;
        messageProducer = null;
        connection = null;
    }
}
