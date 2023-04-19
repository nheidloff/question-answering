package com.ibm.question_answering.proxy;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

@ApplicationScoped
public class CustomHeaderFactory implements ClientHeadersFactory {

    final String PROXY_API_KEY_NOT_SET = "NOT_SET"; 
    String apikey = System.getenv("PROXY_API_KEY");
    
    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("Authorization", "Bearer " + apikey);
        return result;
    }
}