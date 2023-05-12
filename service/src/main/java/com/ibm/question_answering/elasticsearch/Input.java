package com.ibm.question_answering.elasticsearch;

public class Input {
    public Query query;
    public int size;
    
    // https://stackoverflow.com/questions/76228719/jackson-serializer-not-invoked-in-quarkus
    //public Highlight highlight;
}
