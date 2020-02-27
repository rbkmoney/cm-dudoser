package com.rbkmoney.cm.dudoser.config;

import com.rbkmoney.damsel.claim_management.ClaimManagementSrv;
import com.rbkmoney.damsel.messages.MessageServiceSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Slf4j
@Configuration
public class ClientConfig {

    @Bean
    public ClaimManagementSrv.Iface claimManagementClient(@Value("${claimmanagement.client.adapter.url}") Resource resource,
                                                          @Value("${claimmanagement.client.adapter.networkTimeout}") int timeout) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .withNetworkTimeout(timeout)
                .build(ClaimManagementSrv.Iface.class);
    }

    @Bean
    public MessageServiceSrv.Iface messageServiceClient(@Value("${conversations.client.adapter.url}") Resource resource,
                                                        @Value("${conversations.client.adapter.networkTimeout}") int timeout) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .withNetworkTimeout(timeout)
                .build(MessageServiceSrv.Iface.class);
    }
}