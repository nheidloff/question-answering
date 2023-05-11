package com.ibm.question_answering;

import com.ibm.question_answering.primeqa.AnswerDocument;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.maas.AskModelAsAService;
import com.ibm.question_answering.primeqa.AskPrimeQA;

@ApplicationScoped
public class QueryPrimeAndMaaS {
    
    @Inject
    AskPrimeQA askPrimeQA;

    @Inject
    AskModelAsAService askMaaS;

    public Answer query(String query) {
        AnswerDocument[] answerDocuments = askPrimeQA.executeAndReturnRawAnswerDocuments(query);
        return askMaaS.execute(query, answerDocuments);
    }
}