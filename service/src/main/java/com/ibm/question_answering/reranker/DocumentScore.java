package com.ibm.question_answering.reranker;

public class DocumentScore {

    public DocumentScore(Document document, double score) {
        this.document = document;
        this.score = score;
    }
    
    public Document document;
    public double score;
}
