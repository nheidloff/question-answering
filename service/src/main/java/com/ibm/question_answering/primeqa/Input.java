package com.ibm.question_answering.primeqa;

import java.util.List;

public class Input {
    
    public Input(String collectionId, String question, 
        String retrieverId, List<Parameter> retrieverParameters, String retrieverProvenance,
        String readerId, List<Parameter> readerParameters, String readerProvenance) {

        this.collection = new Collection(collectionId);
        this.question = question;
        this.retriever = new Retriever(retrieverId, retrieverProvenance, retrieverParameters);
        this.reader = new Reader(readerId, readerProvenance, readerParameters);
    }

    public String question;
    public Collection collection; 
    public Retriever retriever;
    public Reader reader;
}
