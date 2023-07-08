package com.ibm.question_answering.elasticsearch;

import java.io.IOException;
import javax.inject.Singleton;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Singleton
public class TextExpansionSerializer extends StdSerializer<TextExpansion> {
    
    String vectorSearch = System.getenv("ELASTIC_SEARCH_USE_VECTOR");
    private boolean useVectorSearch = false;
    
    public TextExpansionSerializer() {
        this(null);
    }
  
    public TextExpansionSerializer(Class<TextExpansion> t) {
        super(t);
    }

    @Override
    public void serialize(TextExpansion value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
        if ((vectorSearch != null) && (!vectorSearch.equals(""))) {
            if (vectorSearch.equalsIgnoreCase("true")) {
                useVectorSearch = true;
            }
        }

        if (useVectorSearch == true) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("ml.tokens");
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("model_id", ".elser_model_1");
            jsonGenerator.writeStringField("model_text", value.query);
            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndObject();
        }
    }
}