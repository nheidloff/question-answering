package com.ibm.question_answering.elasticsearch;

import java.io.IOException;
import javax.inject.Singleton;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Singleton
public class HighlightSerializer extends StdSerializer<Highlight> {
    
    String highlightField = System.getenv("ELASTIC_SEARCH_HIGHLIGHT_FIELD");

    public HighlightSerializer() {
        this(null);
    }
  
    public HighlightSerializer(Class<Highlight> t) {
        super(t);
    }

    @Override
    public void serialize(Highlight value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
        if ((highlightField != null) && (!highlightField.equals(""))) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("fields");
            jsonGenerator.writeStartArray("fields", 1);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName(highlightField);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("type", "unified");
            jsonGenerator.writeStringField("require_field_match", "true");
            jsonGenerator.writeNumberField("fragment_size", 800);
            jsonGenerator.writeNumberField("number_of_fragments", 1);
            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
        else {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }
    }
}