package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.exception.ThriftClientException;
import com.rbkmoney.questionary.manage.Head;
import com.rbkmoney.questionary.manage.Questionary;
import com.rbkmoney.questionary.manage.QuestionaryManagerSrv;
import com.rbkmoney.questionary.manage.Reference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionaryService {

    private final QuestionaryManagerSrv.Iface questionaryManager;

    public Questionary getQuestionary(String questionaryId, String partyId) {
        try {
            log.info("Get questionary by id={} and partyId={}", questionaryId, partyId);
            return questionaryManager.get(questionaryId, partyId, Reference.head(new Head())).getQuestionary();
        } catch (TException e) {
            throw new ThriftClientException(String.format("Get questionary '%s' failed", questionaryId), e);
        }
    }

}
