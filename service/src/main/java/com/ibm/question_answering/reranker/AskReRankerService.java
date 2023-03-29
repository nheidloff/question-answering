package com.ibm.question_answering.reranker;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.question_answering.Metrics;

@ApplicationScoped
public class AskReRankerService {
    public AskReRankerService() {}
       
    @Inject
    ReRankerServiceResource discoveryResource;

    @ConfigProperty(name = "experiment.reranker-model") 
    Optional<String> rerankerModelOptionalString;

    @ConfigProperty(name = "experiment.reranker-id") 
    Optional<String> rerankerIdOptionalString;

    @Inject
    Metrics metrics;
    
    /*
    public com.ibm.question_answering.Answer execute(String query) {
        Input input = new Input();
        DocumentScore[][] output = discoveryResource.ask(input);
        return output;
    }
    */

    public DocumentScore[][] executeAndReturnRawAnswer(String query, DocumentScore[] documentsAndScores) {
        if (documentsAndScores == null) {
            return null;
        }
        Input input = new Input();
        
        String[] queries = new String[1];
        queries[0] = query;
        input.queries = queries;

        Parameter[] parameters = new Parameter[1];
        String model;
        if (rerankerModelOptionalString.isPresent()) {
            model = rerankerModelOptionalString.get();
        }
        else {
            model = Parameter.MODEL_DRDECT;
        }
        parameters[0] = new Parameter(model);
        String id;
        if (rerankerIdOptionalString.isPresent()) {
            id = rerankerIdOptionalString.get();
        }
        else {
            id = ReRanker.RERANKER_ID_COLBERT;
        }
        ReRanker reRanker = new ReRanker(id, parameters);
        input.reranker = reRanker;

        DocumentScore[][] documentsAndScoresArray = new DocumentScore[1][documentsAndScores.length];
        documentsAndScoresArray[0] = documentsAndScores;
        input.hitsperquery = documentsAndScoresArray;

        metrics.reRankerStarted(id, model);
        DocumentScore[][] output = discoveryResource.ask(input);
        metrics.reRankerStopped(output);

        return output;
    }
}
