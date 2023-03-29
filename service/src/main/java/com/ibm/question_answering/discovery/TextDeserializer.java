package com.ibm.question_answering.discovery;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class TextDeserializer extends StdDeserializer<com.ibm.question_answering.discovery.Text> { 

    public TextDeserializer() { 
        this(null);
    } 

    public TextDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public com.ibm.question_answering.discovery.Text deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Text output = new Text();
        String[] strings = new String[0];
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.size() > 0) {
            strings = new String[node.size()];
            for (int index = 0; index < node.size(); index++) {
                if (node.get(index).asText() != null) {
                    strings[index] = node.get(index).asText();
                }
            }
        }
        output.text = strings;
        return output;
    }
}
