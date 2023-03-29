package com.ibm.question_answering.discovery;

public class Input {
 
    final static String PREFIX = "text:";

    public Input(String collectionId, String query, int count) {
        //this.query = PREFIX + query;
        this.natural_language_query = PREFIX + query;
        
        this.collection_ids = new String[1];
        this.collection_ids[0] = collectionId;

        this.count = count;
    }
    
    public String collection_ids[];
    
    //public String query;
    public String natural_language_query;

    public int count;
}
