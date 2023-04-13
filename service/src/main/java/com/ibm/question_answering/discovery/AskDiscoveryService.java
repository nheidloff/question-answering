package com.ibm.question_answering.discovery;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.Answer;
import com.ibm.question_answering.Metrics;

@ApplicationScoped
public class AskDiscoveryService {
    public AskDiscoveryService() {}

    final String DISCOVERY_COLLECTION_ID_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "DISCOVERY_COLLECTION_ID", defaultValue = DISCOVERY_COLLECTION_ID_NOT_SET) 
    private String collectionId;
    final static String ERROR_COLLECTION_ID_NOT_SET = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "DISCOVERY_COLLECTION_ID not defined";

    final String DISCOVERY_URL_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "DISCOVERY_URL", defaultValue = DISCOVERY_URL_NOT_SET) 
    private String collectionUrl;
    final static String ERROR_DISCOVERY_URL_NOT_SET = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "DISCOVERY_URL not defined";

    final String DISCOVERY_API_KEY_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "DISCOVERY_API_KEY", defaultValue = DISCOVERY_API_KEY_NOT_SET) 
    private String apiKey;
    final static String ERROR_DISCOVERY_API_KEY_NOT_SET = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "DISCOVERY_API_KEY not defined";

    final String DISCOVERY_INSTANCE_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "DISCOVERY_INSTANCE", defaultValue = DISCOVERY_INSTANCE_NOT_SET) 
    private String instance;
    final static String ERROR_DISCOVERY_INSTANCE_NOT_SET = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "DISCOVERY_INSTANCE not defined";

    final String DISCOVERY_PROJECT_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "DISCOVERY_PROJECT", defaultValue = DISCOVERY_PROJECT_NOT_SET) 
    private String project;
    final static String ERROR_DISCOVERY_PROJECT_NOT_SET = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "DISCOVERY_PROJECT not defined";
  
    final static int DISCOVERY_MAX_OUTPUT_DOCUMENTS = 5;
    @ConfigProperty(name = "experiment.discovery-max-output-documents") 
    Optional<String> maxDocumentsOptionalString;
    
    @Inject
    DiscoveryServiceResource discoveryResource;

    @Inject
    Metrics metrics;
  
    public com.ibm.question_answering.Answer ask(String query) {
        if (collectionId.equalsIgnoreCase(DISCOVERY_COLLECTION_ID_NOT_SET)) {
            System.err.println(ERROR_COLLECTION_ID_NOT_SET);
            throw new RuntimeException(ERROR_COLLECTION_ID_NOT_SET);
        }
        if (collectionUrl.equalsIgnoreCase(DISCOVERY_URL_NOT_SET)) {
            System.err.println(ERROR_DISCOVERY_URL_NOT_SET);
            throw new RuntimeException(ERROR_DISCOVERY_URL_NOT_SET);
        }
        if (apiKey.equalsIgnoreCase(DISCOVERY_API_KEY_NOT_SET)) {
            System.err.println(ERROR_DISCOVERY_API_KEY_NOT_SET);
            throw new RuntimeException(ERROR_DISCOVERY_API_KEY_NOT_SET);
        }
        if (project.equalsIgnoreCase(DISCOVERY_PROJECT_NOT_SET)) {
            System.err.println(ERROR_DISCOVERY_PROJECT_NOT_SET);
            throw new RuntimeException(ERROR_DISCOVERY_PROJECT_NOT_SET);
        }
        if (instance.equalsIgnoreCase(DISCOVERY_INSTANCE_NOT_SET)) {
            System.err.println(ERROR_DISCOVERY_INSTANCE_NOT_SET);
            throw new RuntimeException(ERROR_DISCOVERY_INSTANCE_NOT_SET);
        }
        int maxDocs = DISCOVERY_MAX_OUTPUT_DOCUMENTS;
        if (maxDocumentsOptionalString.isPresent()) {
            try {
                maxDocs = Integer.parseInt(maxDocumentsOptionalString.get());
            } catch (Exception e) {}
        }

        metrics.discoveryStarted(maxDocs);
        Input input = new Input(collectionId, query, maxDocs);
        com.ibm.question_answering.Answer output = discoveryResource.ask(input);
        output = ensureDocumentIdsExist(output);
        metrics.discoveryStopped(output);
        return output;
    }

    // There are two options:
    // 1. Full documents are returned
    // 2. Split smaller chunks are returned
    // If there are no document ids, use the urls as document ids
    // If there are no chunk ids, use the document ids (= urls)
    public Answer ensureDocumentIdsExist(Answer answer) {
        for (int index = 0; index < answer.results.size(); index++) {
            if (answer.results.get(index).document_id == null) {
                answer.results.get(index).document_id = answer.results.get(index).url;
            }
        }
        for (int index = 0; index < answer.results.size(); index++) {
            if (answer.results.get(index).chunckid == null) {
                answer.results.get(index).chunckid = answer.results.get(index).url;
            }
        }
        return answer;
    }
}
