package com.ibm.question_answering.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import javax.inject.Singleton;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

    @Override
    public int priority() {
        return MINIMUM_PRIORITY; 
    }

    public void customize(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Highlight.class, new HighlightSerializer());
        module.addSerializer(Filter.class, new FilterSerializer());
        module.addDeserializer(HighlightResult.class, new HighlightResultDeserializer());
        module.addDeserializer(Document.class, new DocumentDeserializer());
        mapper.registerModule(module);
    }
}
