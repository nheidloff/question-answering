package com.ibm.question_answering.elasticsearch;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class DocumentDeserializer extends StdDeserializer<Document> { 

    String text = System.getenv("ELASTIC_SEARCH_FIELD_ARRAY_TEXT");
    String id = System.getenv("ELASTIC_SEARCH_FIELD_SINGLE_ID");
    String title = System.getenv("ELASTIC_SEARCH_FIELD_SINGLE_TITLE");
    String urlPart1 = System.getenv("ELASTIC_SEARCH_FIELD_RESULT_SINGLE_1");
    String urlPart2 = System.getenv("ELASTIC_SEARCH_FIELD_RESULT_SINGLE_2");

    public DocumentDeserializer() { 
        this(null);
    } 

    public DocumentDeserializer(Class<Document> vc) { 
        super(vc); 
    }

    @Override
    public Document deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Document output = new Document();
        JsonNode node = jp.getCodec().readTree(jp); 
        try {
            output.id = node.get(id).asText();
        } catch (Exception e) {
        }
        try {
            output.title = node.get(title).asText();
        } catch (Exception e) {
        }
        try {
            output.urlField1 = node.get(urlPart1).asText();
        } catch (Exception e) {
        }
        try {
            output.urlField2 = node.get(urlPart2).asText();
        } catch (Exception e) {
        }
        try {
            JsonNode textNode = node.get(text);
            String[] strings = new String[textNode.size()];
            for (int index = 0; index < textNode.size(); index++) {   
                strings[index] = textNode.get(index).asText();
            }   
            output.text = strings;
        } catch (Exception e) {
        }
        return output;
    }
}
