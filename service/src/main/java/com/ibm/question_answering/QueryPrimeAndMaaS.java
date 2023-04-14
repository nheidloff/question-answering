package com.ibm.question_answering;

import com.ibm.question_answering.primeqa.AnswerDocument;
import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
        return queryMaaS(answerDocuments, query);
    }

    public Answer queryMaaS(AnswerDocument[] answerDocuments, String query) {
        
        Answer answer = askMaaS.execute(query, answerDocuments);

        if (answerDocuments != null) {
            answer.matching_results = answerDocuments.length;
            ArrayList<Result> results = new ArrayList<Result>();
            results.add(answer.results.get(0));
            for (int index = 0; index < answerDocuments.length; index++) {
                results.add(askPrimeQA.getAnswerDocument(answerDocuments[index]));
            }
            answer.results = results;
        }
        return answer;
    }
}