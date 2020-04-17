package com.rbkmoney.cm.dudoser.config;

import com.rbkmoney.cm.dudoser.meta.UserIdentityEmailExtensionKit;
import com.rbkmoney.cm.dudoser.meta.UserIdentityIdExtensionKit;
import com.rbkmoney.cm.dudoser.meta.UserIdentityRealmExtensionKit;
import com.rbkmoney.cm.dudoser.meta.UserIdentityUsernameExtensionKit;
import com.rbkmoney.damsel.claim_management.ClaimManagementSrv;
import com.rbkmoney.damsel.messages.MessageServiceSrv;
import com.rbkmoney.file.storage.FileStorageSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Configuration
public class ClientConfig {

    @Bean
    public ClaimManagementSrv.Iface claimManagementClient(@Value("${claimmanagement.client.adapter.url}") Resource resource,
                                                          @Value("${claimmanagement.client.adapter.networkTimeout}") int timeout) throws IOException {
        return new THSpawnClientBuilder()
                .withMetaExtensions(
                        Arrays.asList(
                                UserIdentityIdExtensionKit.INSTANCE,
                                UserIdentityEmailExtensionKit.INSTANCE,
                                UserIdentityUsernameExtensionKit.INSTANCE,
                                UserIdentityRealmExtensionKit.INSTANCE
                        )
                )
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

    @Bean
    public FileStorageSrv.Iface fileStorageClient(@Value("${filestorage.client.adapter.url}") Resource resource,
                                                  @Value("${filestorage.client.adapter.networkTimeout}") int timeout) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .withNetworkTimeout(timeout)
                .build(FileStorageSrv.Iface.class);
    }

}
