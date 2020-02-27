package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.damsel.messages.Conversation;

public interface MessageService {

    Conversation getConversation(String conversationId);

}
