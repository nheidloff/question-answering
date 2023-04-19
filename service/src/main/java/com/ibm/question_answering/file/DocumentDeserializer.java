package com.ibm.question_answering.file;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class DocumentDeserializer extends StdDeserializer<com.ibm.question_answering.file.Document> { 

    public DocumentDeserializer() { 
        this(null);
    } 

    public DocumentDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public com.ibm.question_answering.file.Document deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Document output = new Document();
        String[] strings = null;
        JsonNode node = jp.getCodec().readTree(jp);
        if (node != null) {
            output.title = node.get("title").asText();
            output.url = node.get("url").asText();

            JsonNode textNode = node.get("text");
            if (textNode != null) {
                if (textNode.size() > 0) {
                    strings = new String[textNode.size()];
                    for (int index = 0; index < textNode.size(); index++) {
                        if (textNode.get(index).asText() != null) {
                            strings[index] = textNode.get(index).asText();
                        }
                    }
                }
            }
            output.text = strings;
        }
        return output;
    }
}
