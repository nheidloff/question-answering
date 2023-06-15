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
    ReRankerServiceResource rerankerResource;

    @Inject
    com.ibm.question_answering.reranker2.ReRankerServiceResource reranker2Resource;

    @Inject
    Metrics metrics;

    String reranker2 = System.getenv("RERANKER2");
    private boolean useReRanker2 = false;
    
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
        if ((reranker2 != null) && (!reranker2.equals(""))) {
            if (reranker2.equalsIgnoreCase("true")) {
                useReRanker2 = true;
            }
        }
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

        String[] queries = new String[1];
        queries[0] = query;
        DocumentScore[][] output;
        if (useReRanker2 == false) {
            Input input = new Input();
            input.queries = queries;
            Parameter[] parameters = new Parameter[1];
            parameters[0] = new Parameter(rerankerModel);
            ReRanker reRanker = new ReRanker(rerankerId, parameters);
            input.reranker = reRanker;
            DocumentScore[][] documentsAndScoresArray = new DocumentScore[1][documentsAndScores.length];
            documentsAndScoresArray[0] = documentsAndScores;
            input.hitsperquery = documentsAndScoresArray;
            metrics.reRankerStarted(rerankerId, rerankerModel);
            output = rerankerResource.ask(input);
        }
        else {
            com.ibm.question_answering.reranker2.Input input = new com.ibm.question_answering.reranker2.Input();
            input.queries = queries;
            input.passages = convertDocumentScoresToStringArrayArray(documentsAndScores);
            metrics.reRankerStarted(rerankerId, rerankerModel);
            com.ibm.question_answering.reranker2.Response response = reranker2Resource.ask(input);
            output = convertResponseToDocumentScoreArrayArray(response, documentsAndScores);
        }
        metrics.reRankerStopped(output);
        return output;
    }

    private String[][] convertDocumentScoresToStringArrayArray(DocumentScore[] documentsAndScores) {
        String[][] output = new String[1][documentsAndScores.length];
        for (int index = 0; index < documentsAndScores.length; index++) {
            output[0][index] = documentsAndScores[index].document.title + " " +
                documentsAndScores[index].document.text;;
        }
        return output;
    }

    private DocumentScore[][] convertResponseToDocumentScoreArrayArray(com.ibm.question_answering.reranker2.Response response, 
        DocumentScore[] input) {
        int[] order = response.order[0];
        DocumentScore[][] output = new DocumentScore[1][order.length];
        for (int index = 0; index < order.length; index++) {
            output[0][index] = new DocumentScore();
            output[0][index].document = new Document();
            output[0][index].document.text = input[order[index]].document.text;
            output[0][index].document.title = input[order[index]].document.title;
            output[0][index].document.document_id = input[order[index]].document.document_id;
        }
        return output;
    }
}
