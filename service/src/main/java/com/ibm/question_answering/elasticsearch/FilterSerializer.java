package com.ibm.question_answering.elasticsearch;

import java.io.IOException;
import javax.inject.Singleton;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Singleton
public class FilterSerializer extends StdSerializer<Filter> {
    
    String filterName1;
    String filterName2;
    String filterName3;
    String filterValue1;
    String filterValue2;
    String filterValue3;

    public FilterSerializer() {
        this(null);
    }
  
    public FilterSerializer(Class<Filter> t) {
        super(t);
    }

    @Override
    public void serialize(Filter value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
        String envVar = System.getenv("ELASTIC_SEARCH_FILTER_NAME_1");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterName1 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_NAME_2");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterName2 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_NAME_3");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterName3 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_VALUE_1");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterValue1 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_VALUE_2");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterValue2 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_VALUE_3");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterValue3 = envVar;   

        }

        System.out.println("niklas filterName1 " + filterName1);
        System.out.println("niklas filterName2 " + filterName2);
        System.out.println("niklasfilterName3 " + filterName3);

        if (filterName3 != null) {
            jsonGenerator.writeStartArray();
                jsonGenerator.writeStartObject();
                    jsonGenerator.writeFieldName("term");
                        jsonGenerator.writeStartObject();
                            jsonGenerator.writeStringField(filterName1, filterValue1);
                    jsonGenerator.writeEndObject();
                jsonGenerator.writeEndObject();
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("term");
                    jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(filterName2, filterValue2);
                    jsonGenerator.writeEndObject();
                jsonGenerator.writeEndObject();
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("term");
                    jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(filterName3, filterValue3);
                    jsonGenerator.writeEndObject();
                jsonGenerator.writeEndObject();
            jsonGenerator.writeEndArray();
        }
        else if (filterName2 != null) {
            jsonGenerator.writeStartArray();
                jsonGenerator.writeStartObject();
                    jsonGenerator.writeFieldName("term");
                        jsonGenerator.writeStartObject();
                            jsonGenerator.writeStringField(filterName1, filterValue1);
                    jsonGenerator.writeEndObject();
                jsonGenerator.writeEndObject();
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("term");
                    jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(filterName2, filterValue2);
                    jsonGenerator.writeEndObject();
                jsonGenerator.writeEndObject();
            jsonGenerator.writeEndArray();
        } 
        else if (filterName1 != null) {
            jsonGenerator.writeStartArray();
                jsonGenerator.writeStartObject();
                    jsonGenerator.writeFieldName("term");
                        jsonGenerator.writeStartObject();
                            jsonGenerator.writeStringField(filterName1, filterValue1);
                    jsonGenerator.writeEndObject();
                jsonGenerator.writeEndObject();
            jsonGenerator.writeEndArray();
        } 
        else {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }
    }
}