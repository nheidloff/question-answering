package com.ibm.question_answering.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Input {
    public Query query;
    public int size;
    
    // TODO
    @JsonIgnore
    public Highlight highlight;
}
