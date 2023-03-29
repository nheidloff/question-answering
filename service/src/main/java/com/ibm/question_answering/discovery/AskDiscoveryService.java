package com.ibm.question_answering.discovery;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.question_answering.Metrics;

@ApplicationScoped
public class AskDiscoveryService {
    public AskDiscoveryService() {}

    final String DISCOVERY_COLLECTION_ID_NOT_SET = "NOT_SET";
    
    @ConfigProperty(name = "DISCOVERY_COLLECTION_ID", defaultValue = DISCOVERY_COLLECTION_ID_NOT_SET) 
    private String collectionId;
    
    @Inject
    DiscoveryServiceResource discoveryResource;

    @Inject
    Metrics metrics;
  
    public com.ibm.question_answering.Answer ask(String query, int count) {
        metrics.discoveryStarted();
        Input input = new Input(collectionId, query, count);
        com.ibm.question_answering.Answer output = discoveryResource.ask(input);
        metrics.discoveryStopped(output);
        return output;
    }
}
