package com.ibm.question_answering;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Data {
    
    @JsonIgnore
    private final String PREFIX = "text:";
    
    public String query;

    @JsonIgnore
    public String getTextQuery() {
        String output = "";
        if (query != null) {
            if (query.startsWith(PREFIX)) {
                output = query.substring(PREFIX.length(), query.length());
            }
        }
        return output;
    }
}
