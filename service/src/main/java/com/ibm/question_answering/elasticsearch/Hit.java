package com.ibm.question_answering.elasticsearch;

public class Hit {
    public String _id;
    public float _score;
    public Document _source;
    public HighlightResult highlight;  
}
