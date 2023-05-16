package com.ibm.question_answering.elasticsearch;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class HighlightResultDeserializer extends StdDeserializer<HighlightResult> { 

    String highlightField = System.getenv("ELASTIC_SEARCH_HIGHLIGHT_FIELD");

    public HighlightResultDeserializer() { 
        this(null);
    } 

    public HighlightResultDeserializer(Class<HighlightResult> vc) { 
        super(vc); 
    }

    @Override
    public HighlightResult deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        HighlightResult output = new HighlightResult();
        if ((highlightField != null) && (!highlightField.equals(""))) {
            String[] strings = new String[0];
            JsonNode node = jp.getCodec().readTree(jp);            
            if (node.size() > 0) {
                JsonNode nodeHighlightField = node.get(highlightField);
                if (nodeHighlightField != null) {
                    strings = new String[nodeHighlightField.size()];
                    for (int index = 0; index < nodeHighlightField.size(); index++) {   
                        strings[index] = nodeHighlightField.get(index).asText();
                    }                
                }
            }
            output.text = strings;
        }
        return output;
    }
}
