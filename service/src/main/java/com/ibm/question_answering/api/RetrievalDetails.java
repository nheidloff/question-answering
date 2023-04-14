package com.ibm.question_answering.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RetrievalDetails {

    @JsonIgnore
    public static final String DOCUMENT_RETRIEVAL_STATEGY_UNTRAINED = "untrained";
    @JsonIgnore
    public static final String DOCUMENT_RETRIEVAL_STATEGY_LLM = "llm";

    public RetrievalDetails(boolean highConfidence) {
        if (highConfidence == true) {
            document_retrieval_strategy = DOCUMENT_RETRIEVAL_STATEGY_LLM;
        }
        else {
            document_retrieval_strategy = DOCUMENT_RETRIEVAL_STATEGY_UNTRAINED;
        }
    }

    public String document_retrieval_strategy;   
}
