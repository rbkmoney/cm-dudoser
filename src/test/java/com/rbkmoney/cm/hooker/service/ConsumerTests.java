package com.rbkmoney.cm.hooker.service;

import com.rbkmoney.cm.hooker.config.AbstractKafkaConfig;
import com.rbkmoney.cm.hooker.domain.Mail;
import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.kafka.common.serialization.ThriftSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Mockito.*;

@Slf4j
public class ConsumerTests extends AbstractKafkaConfig {

    @Value("${kafka.topics.claim-event-sink.id}")
    public String topic;

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @MockBean
    private RetryService retryService;

    @MockBean
    private MailService<ClaimStatusChanged> statusChangedMailService;

    @Test
    public void paymentProcessingKafkaListenerTest() throws InterruptedException {
        ClaimStatusChanged claimStatusChanged = getClaimStatusChanged();

        Event event = new Event();
        event.setUserInfo(getUserInfo());
        event.setOccuredAt(LocalDateTime.now().toString());
        event.setChange(Change.status_changed(claimStatusChanged));

        when(statusChangedMailService.buildMail(eq(claimStatusChanged), anyString(), anyLong())).thenReturn(Mail.builder().build());
        doNothing().when(retryService).repeatableSendMessage(any());

        try {
            DefaultKafkaProducerFactory<String, Event> producerFactory = createProducerFactory();

            KafkaTemplate<String, Event> kafkaTemplate = new KafkaTemplate<>(producerFactory);

            TimeUnit.SECONDS.sleep(1);

            kafkaTemplate.send(
                    topic,
                    random(String.class),
                    event
            )
                    .get();

            TimeUnit.SECONDS.sleep(1);
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
        }

        verify(statusChangedMailService, times(1)).buildMail(eq(claimStatusChanged), anyString(), anyLong());
        verify(retryService, times(1)).repeatableSendMessage(any());
    }

    private UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setType(UserType.internal_user(new InternalUser()));
        userInfo.setId(UUID.randomUUID().toString());
        userInfo.setUsername("asd");
        userInfo.setEmail("asd@sad.com");
        return userInfo;
    }

    private ClaimStatusChanged getClaimStatusChanged() {
        return new ClaimStatusChanged(UUID.randomUUID().toString(), 1, ClaimStatus.pending(new ClaimPending()), 1, LocalDateTime.now().toString());
    }

    private <T> DefaultKafkaProducerFactory<String, T> createProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client_id");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new ThriftSerializer<Event>().getClass().getName());
        return new DefaultKafkaProducerFactory<>(props);
    }
}
