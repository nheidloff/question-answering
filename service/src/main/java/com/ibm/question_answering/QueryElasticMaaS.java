package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.elasticsearch.AskElasticService;

@ApplicationScoped
public class QueryElasticMaaS {
    @Inject
    AskElasticService askElastic;

    public Answer query(String query) { 
        return askElastic.search(query);
    }

}
