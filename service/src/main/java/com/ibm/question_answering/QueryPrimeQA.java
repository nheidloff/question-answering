package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.ibm.question_answering.primeqa.AskPrimeQA;

@ApplicationScoped
public class QueryPrimeQA {
    
    @Inject
    AskPrimeQA askPrimeQA;

    public Answer query(String query) {
        
        return askPrimeQA.execute(query);   
    }
}
