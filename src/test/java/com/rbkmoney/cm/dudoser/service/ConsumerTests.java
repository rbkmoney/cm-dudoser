package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.config.AbstractKafkaConfig;
import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.exception.MailSendException;
import com.rbkmoney.cm.dudoser.exception.NotFoundException;
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
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Mockito.*;

@Slf4j
public class ConsumerTests extends AbstractKafkaConfig {

    @Value("${kafka.topics.claim-event-sink.id}")
    public String topic;

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @MockBean
    private RetryableSenderService retryableSenderService;

    @MockBean
    private MessageBuilderService<ClaimStatusChanged> statusMessageBuilder;

    @Test
    public void notFoundExceptionIsSneakyThrowTest() {
        ClaimStatusChanged claimStatusChanged = getClaimStatusChanged();

        Event event = new Event();
        event.setUserInfo(getUserInfo());
        event.setOccuredAt(LocalDateTime.now().toString());
        event.setChange(Change.status_changed(claimStatusChanged));

        when(statusMessageBuilder.build(eq(claimStatusChanged), any(), anyString(), anyLong())).thenThrow(NotFoundException.class);
        doNothing().when(retryableSenderService).sendToMail(any());

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

        verify(statusMessageBuilder, times(1)).build(eq(claimStatusChanged), any(), anyString(), anyLong());
        // проверяем что ивент из кафки был корректно отправлен сообщением на почтовый сервер
        verify(retryableSenderService, times(0)).sendToMail(any());
    }

    @Test
    public void correctFlowTest() {
        ClaimStatusChanged claimStatusChanged = getClaimStatusChanged();

        Event event = new Event();
        event.setUserInfo(getUserInfo());
        event.setOccuredAt(LocalDateTime.now().toString());
        event.setChange(Change.status_changed(claimStatusChanged));

        when(statusMessageBuilder.build(eq(claimStatusChanged), any(), anyString(), anyLong())).thenReturn(Message.builder().build());
        doNothing().when(retryableSenderService).sendToMail(any());

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

        verify(statusMessageBuilder, times(1)).build(eq(claimStatusChanged), any(), anyString(), anyLong());
        // проверяем что ивент из кафки был корректно отправлен сообщением на почтовый сервер
        verify(retryableSenderService, times(1)).sendToMail(any());
    }

    @Test
    public void errorRetryTest() {
        int countRetries = 5;
        AtomicInteger atomicInt = new AtomicInteger(0);
        ClaimStatusChanged claimStatusChanged = getClaimStatusChanged();

        Event event = new Event();
        event.setUserInfo(getUserInfo());
        event.setOccuredAt(LocalDateTime.now().toString());
        event.setChange(Change.status_changed(claimStatusChanged));

        when(statusMessageBuilder.build(eq(claimStatusChanged), any(), anyString(), anyLong())).thenReturn(Message.builder().build());
        // на countRetries попытке мок корректно обработает вызов, в предыдущих попытках будет вынуждать кафку ретраить обработку ивента
        doAnswer(
                invocation -> {
                    int increment = atomicInt.getAndIncrement();
                    if (increment < countRetries - 1) {
                        throw new MailSendException();
                    }
                    return true;
                }
        ).when(retryableSenderService).sendToMail(any());

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

            TimeUnit.SECONDS.sleep(6);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        verify(statusMessageBuilder, times(countRetries)).build(eq(claimStatusChanged), any(), anyString(), anyLong());
        // проверяем, что кафка пыталась обработать ивент такое количество раз, которое задано моку
        verify(retryableSenderService, times(countRetries)).sendToMail(any());
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
