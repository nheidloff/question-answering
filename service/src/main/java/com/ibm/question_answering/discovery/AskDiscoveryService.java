package com.ibm.question_answering.discovery;

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

    final String DISCOVERY_MAX_OUTPUT_DOCUMENTS_NOT_SET = "NOT_SET";
    
    @ConfigProperty(name = "experiment.discovery-max-output-documents", defaultValue = DISCOVERY_MAX_OUTPUT_DOCUMENTS_NOT_SET) 
    private String maxDocuments;
    
    @Inject
    DiscoveryServiceResource discoveryResource;

    @Inject
    Metrics metrics;
  
    public com.ibm.question_answering.Answer ask(String query) {
        int maxDocs = 5;
        try {
            maxDocs =  Integer.parseInt(maxDocuments);
        } 
        catch (Exception e) {}

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
