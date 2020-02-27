package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.service.MessageService;
import com.rbkmoney.damsel.messages.Conversation;
import com.rbkmoney.damsel.messages.ConversationFilter;
import com.rbkmoney.damsel.messages.GetConversationResponse;
import com.rbkmoney.damsel.messages.MessageServiceSrv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageServiceSrv.Iface messageServiceClient;

    @Override
    public Conversation getConversation(String conversationId) {
        try {
            log.info("Trying to get \"Conversation\" from thrift client, conversationId={}", conversationId);

            GetConversationResponse response = messageServiceClient.getConversations(List.of(conversationId), new ConversationFilter());

            if (response == null || response.getConversations() == null || response.getConversations().size() != 1) {
                throw new RuntimeException(String.format("Conversation's size must be = 1, conversationId=%s", conversationId));
            }

            return response.getConversations().get(0);
        } catch (TException ex) {
            log.error("Failed to get \"Conversation\" from thrift client, conversationId={}", conversationId, ex);
            throw new RuntimeException("Some problem with \"messageServiceClient\"", ex);
        }
    }
}
