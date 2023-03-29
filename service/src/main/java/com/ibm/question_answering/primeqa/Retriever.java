package com.ibm.question_answering.primeqa;

import java.util.List;

public class Retriever {

    public Retriever(String retriever_id, String provenance, List<Parameter> parameters) {
        this.retriever_id = retriever_id;
        if (retriever_id == null) {
            this.retriever_id = RETRIEVER_ID_WATSON_DISCOVERY;
        } else if (retriever_id.equals("")) {
            this.retriever_id = RETRIEVER_ID_WATSON_DISCOVERY;
        }
        this.parameters = parameters;
        this.provenance = provenance;
        if (provenance == null) {
            this.provenance = PROVENANCE_WATSON_DISCOVERY;
        } else if (provenance.equals("")) {
            this.provenance = PROVENANCE_WATSON_DISCOVERY;
        }
    }
    
    public final static String PROVENANCE_WATSON_DISCOVERY = "Watson Discovery";
    public final static String RETRIEVER_ID_WATSON_DISCOVERY = "WatsonDiscovery";

    public String retriever_id;
    public String provenance;
    public List<Parameter> parameters;
}
