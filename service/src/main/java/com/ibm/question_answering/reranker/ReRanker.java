package com.ibm.question_answering.reranker;

public class ReRanker {

    public final static String RERANKER_ID_COLBERT = "ColBERTReranker";

    public ReRanker(String reranker_id, Parameter[] parameters) {
        this.reranker_id = reranker_id;
        if (reranker_id == null) {
            this.reranker_id = RERANKER_ID_COLBERT;
        }
        this.parameters = parameters;
    }
    
    public String reranker_id;
    public Parameter[] parameters;
}
