package com.rbkmoney.cm.dudoser.config;

import com.rbkmoney.cm.dudoser.CMDudoserApplication;
import com.rbkmoney.easyway.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FailureDetectingExternalResource;

import java.util.function.Consumer;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CMDudoserApplication.class, webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = AbstractKafkaConfig.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractKafkaConfig extends AbstractTestUtils {

    private static TestContainers testContainers = TestContainersBuilder.builderWithTestContainers(TestContainersParameters::new)
            .addKafkaTestContainer()
            .build();

    @ClassRule
    public static final FailureDetectingExternalResource resource = new FailureDetectingExternalResource() {

        @Override
        protected void starting(Description description) {
            testContainers.startTestContainers();
        }

        @Override
        protected void failed(Throwable e, Description description) {
            log.warn("Test Container running was failed ", e);
        }

        @Override
        protected void finished(Description description) {
            testContainers.stopTestContainers();
        }
    };

    public static class Initializer extends ConfigFileApplicationContextInitializer {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            super.initialize(configurableApplicationContext);
            TestPropertyValues.of(
                    testContainers.getEnvironmentProperties(
                            getEnvironmentPropertiesConsumer()
                    )
            )
                    .applyTo(configurableApplicationContext);
        }
    }

    private static Consumer<EnvironmentProperties> getEnvironmentPropertiesConsumer() {
        return environmentProperties -> {
            environmentProperties.put("kafka.topics.claim-event-sink.enabled", "true");
            testContainers.getKafkaTestContainer().ifPresent(
                    c -> environmentProperties.put("kafka.bootstrap.servers", c.getBootstrapServers())
            );

        };
    }
}
