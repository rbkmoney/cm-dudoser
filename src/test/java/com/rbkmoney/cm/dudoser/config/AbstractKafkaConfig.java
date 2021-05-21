package com.rbkmoney.cm.dudoser.config;

import com.rbkmoney.cm.dudoser.CMDudoserApplication;
import com.rbkmoney.damsel.claim_management.Event;
import com.rbkmoney.easyway.AbstractTestUtils;
import com.rbkmoney.kafka.common.serialization.ThriftSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;

import java.util.HashMap;
import java.util.Map;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CMDudoserApplication.class, webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = AbstractKafkaConfig.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractKafkaConfig extends AbstractTestUtils {

    private static final String CONFLUENT_PLATFORM_VERSION = "5.0.1";
    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(CONFLUENT_PLATFORM_VERSION).withEmbeddedZookeeper();
    @Value("${kafka.topics.claim-event-sink.id}")
    public String topic;

    protected <T> Producer<String, T> createKafkaProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client_id");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new ThriftSerializer<Event>().getClass().getName());
        return new KafkaProducer<>(props);
    }

    protected void produceMessageToEventSink(Event event) {
        try (Producer<String, Event> producer = createKafkaProducer()) {
            ProducerRecord<String, Event> producerRecord = new ProducerRecord<>(
                    topic,
                    random(String.class),
                    event);
            producer.send(producerRecord).get();
            log.info("produceMessageToEventSink() sinkEvent: {}", event);
        } catch (Exception e) {
            log.error("Error when produceMessageToEventSink e:", e);
        }
    }

    public static class Initializer extends ConfigFileApplicationContextInitializer {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "kafka.bootstrap.servers=" + kafka.getBootstrapServers(),
                    "kafka.topics.claim-event-sink.enabled=true",
                    "kafka.error-handler.sleep-time-seconds=1"
            )
                    .applyTo(configurableApplicationContext.getEnvironment());
            kafka.start();
        }
    }

}
