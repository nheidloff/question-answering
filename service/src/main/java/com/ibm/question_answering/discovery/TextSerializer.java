package com.ibm.question_answering.discovery;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TextSerializer extends StdSerializer<Text> {
    
    public TextSerializer() {
        this(null);
    }
  
    public TextSerializer(Class<Text> t) {
        super(t);
    }

    @Override
    public void serialize(Text value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {

        String[] strings = new String[0];
        if (value != null) {
            if (value.text != null) {
                if (value.text.length > 0) {
                    strings = value.text;
                }
            }
        }

        jsonGenerator.writeArray(strings, 0, strings.length);
    }
}