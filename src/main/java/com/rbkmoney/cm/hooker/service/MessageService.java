package com.rbkmoney.cm.hooker.service;

import com.rbkmoney.damsel.messages.Conversation;

public interface MessageService {

    Conversation getConversation(String conversationId);

}
