package com.ibm.question_answering.discovery;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import java.util.Base64;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

@ApplicationScoped
public class CustomHeaderFactory implements ClientHeadersFactory {

    @ConfigProperty(name = "DISCOVERY_API_KEY", defaultValue = "") 
    private String apiKey;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        String toBeEncoded = "apikey:" + apiKey;
        result.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(toBeEncoded.getBytes()));
        return result;
    }
}