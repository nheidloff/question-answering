package com.ibm.question_answering.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Query {
    // TODO
    @JsonIgnore
    public Bool bool;

    public TextExpansion text_expansion;
}
