package com.ibm.question_answering.elasticsearch;

import java.io.IOException;
import javax.inject.Singleton;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Singleton
public class HighlightSerializer extends StdSerializer<Highlight> {
    
    public HighlightSerializer() {
        this(null);
    }
  
    public HighlightSerializer(Class<Highlight> t) {
        super(t);
    }

    // not invoked
    // https://stackoverflow.com/questions/76228719/jackson-serializer-not-invoked-in-quarkus
    @Override
    public void serialize(Highlight value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
        //System.out.println("nik serialize");
        jsonGenerator.writeStringField("niklas", "test");   
    }
}