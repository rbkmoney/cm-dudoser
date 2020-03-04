package com.rbkmoney.cm.dudoser.config;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateConfig {

    @Bean
    public VelocityEngine claimManagementTemplateEngine() {
        String resourceLoader = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";

        VelocityEngine engine = getVelocityEngine(resourceLoader);

        engine.init();
        return engine;
    }

    private VelocityEngine getVelocityEngine(String resourceLoader) {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty("resource.loader", "class");
        engine.setProperty("class.resource.loader.class", resourceLoader);
        return engine;
    }
}
