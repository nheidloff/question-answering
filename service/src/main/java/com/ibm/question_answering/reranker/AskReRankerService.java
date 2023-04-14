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

    @Inject
    Metrics metrics;
    
    final String RERANKER_URL_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "RERANKER_URL", defaultValue = RERANKER_URL_NOT_SET) 
    private String rerankerUrl;
    final static String ERROR_RERANKER_URL_NOT_SET = ReRankerExceptionMapper.ERROR_RERANKER_PREFIX + "RERANKER_URL not defined";

    @ConfigProperty(name = "EXPERIMENT_RERANKER_MODEL", defaultValue = "/store/checkpoints/drdecr/DrDecr.dnn") 
    private String rerankerModel;
    final static String ERROR_RERANKER_MODEL_NOT_SET = ReRankerExceptionMapper.ERROR_RERANKER_PREFIX + "RERANKER_MODEL not defined";

    @ConfigProperty(name = "EXPERIMENT_RERANKER_ID", defaultValue = "ColBERTReranker") 
    private String rerankerId;
    final static String ERROR_RERANKER_ID_NOT_SET = ReRankerExceptionMapper.ERROR_RERANKER_PREFIX + "RERANKER_ID not defined";

    final static int RERANKER_MAX_INPUT_DOCUMENTS = 20;
    @ConfigProperty(name = "EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS") 
    Optional<String> maxInputDocumentsOptionalString;

    public DocumentScore[][] executeAndReturnRawAnswer(String query, DocumentScore[] documentsAndScores) {
        if (rerankerUrl.equalsIgnoreCase(RERANKER_URL_NOT_SET)) {
            System.err.println(ERROR_RERANKER_URL_NOT_SET);
            throw new RuntimeException(ERROR_RERANKER_URL_NOT_SET);
        }
        int maxInputDocuments = RERANKER_MAX_INPUT_DOCUMENTS;
        if (maxInputDocumentsOptionalString.isPresent()) {
            try {
                maxInputDocuments = Integer.parseInt(maxInputDocumentsOptionalString.get());
            } catch (Exception e) {}
        }
        if (documentsAndScores == null) {
            throw new RuntimeException(ReRankerExceptionMapper.ERROR_RERANKER_OTHER);
        }
 
        metrics.setRRAmountInputDocuments(documentsAndScores.length, maxInputDocuments);
        if (maxInputDocuments < documentsAndScores.length) {
            DocumentScore[] documentsAndScoresOrg = documentsAndScores;
            documentsAndScores = new DocumentScore[maxInputDocuments];
            System.arraycopy(documentsAndScoresOrg, 0, documentsAndScores, 0, maxInputDocuments);
        }
        Input input = new Input();
        String[] queries = new String[1];
        queries[0] = query;
        input.queries = queries;
        Parameter[] parameters = new Parameter[1];
        parameters[0] = new Parameter(rerankerModel);
        ReRanker reRanker = new ReRanker(rerankerId, parameters);
        input.reranker = reRanker;
        DocumentScore[][] documentsAndScoresArray = new DocumentScore[1][documentsAndScores.length];
        documentsAndScoresArray[0] = documentsAndScores;
        input.hitsperquery = documentsAndScoresArray;

        metrics.reRankerStarted(rerankerId, rerankerModel);
        DocumentScore[][] output = discoveryResource.ask(input);
        metrics.reRankerStopped(output);

        return output;
    }
}
