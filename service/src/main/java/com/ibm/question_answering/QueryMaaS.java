package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.maas.AskModelAsAService;

import io.smallrye.mutiny.Multi;

@ApplicationScoped
public class QueryMaaS {

    @Inject
    AskModelAsAService askMaaS;

    public Answer query(String prompt) {  
        return askMaaS.execute(prompt);
    }  

    public Multi<com.ibm.question_answering.maas.Answer> queryAsStream(String prompt) {
        return askMaaS.executeAsStream(prompt);
    }
}