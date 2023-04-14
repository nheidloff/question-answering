package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.discovery.AskDiscoveryService;

@ApplicationScoped
public class QueryDiscovery {
    
    @Inject
    AskDiscoveryService askDiscoveryService;

    public Answer query(String query) {
        
        return askDiscoveryService.ask(query);   
    }
}
