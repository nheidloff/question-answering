package com.ibm.question_answering.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import javax.inject.Singleton;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

    @Override
    public int priority() {
        return DEFAULT_PRIORITY + 200; 
    }

    public void customize(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Text.class, new TextDeserializer());
        module.addSerializer(Text.class, new TextSerializer());
        mapper.registerModule(module);
    }
}
