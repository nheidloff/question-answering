package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.elasticsearch.AskElasticService;
import com.ibm.question_answering.maas.AskModelAsAService;

@ApplicationScoped
public class QueryElasticMaaS {

    @Inject
    AskElasticService askElastic;

    @Inject
    AskModelAsAService askMaaS;
    
    @Inject
    Metrics metrics;

    public Answer query(String query) { 

        // 1. Elastic
        com.ibm.question_answering.api.Answer answer = askElastic.search(query);  
        if ((answer == null) || (answer.matching_results < 1)) {
            return MockAnswers.getEmptyAnswer();
        }
        
        // 2. MaaS
        Answer output = askMaaS.execute(query, answer);
        metrics.maaSStopped(output);
        return output;
    }
}