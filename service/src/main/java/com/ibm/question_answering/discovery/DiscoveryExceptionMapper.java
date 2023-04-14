package com.ibm.question_answering.discovery;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class DiscoveryExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    public final static String ERROR_DISCOVERY_PREFIX = "Watson Discovery error: ";
    public final static String ERROR_DISCOVERY_500 = ERROR_DISCOVERY_PREFIX + "The remote service responded with HTTP 500";
    public final static String ERROR_DISCOVERY_405 = ERROR_DISCOVERY_PREFIX + "The remote service responded with HTTP 405";
    public final static String ERROR_DISCOVERY_401 = ERROR_DISCOVERY_PREFIX + "The remote service responded with HTTP 401";
    public final static String ERROR_DISCOVERY_OTHER = ERROR_DISCOVERY_PREFIX + "Other";
    public final static String ERROR_DISCOVERY_UNEXPECTED = ERROR_DISCOVERY_PREFIX + "Unexpected";

	@Override
    public RuntimeException toThrowable(Response response) {        
        
        if (response.getStatus() == 500) {            
            System.err.println(ERROR_DISCOVERY_500);
            throw new RuntimeException(ERROR_DISCOVERY_500);
        }
        if (response.getStatus() == 405) {
            System.err.println(ERROR_DISCOVERY_405);
            throw new RuntimeException(ERROR_DISCOVERY_405);
        }
        if (response.getStatus() == 401) {
            System.out.println(ERROR_DISCOVERY_401);
            throw new RuntimeException(ERROR_DISCOVERY_401);
        }
        System.err.println(ERROR_DISCOVERY_OTHER);
        throw new RuntimeException(ERROR_DISCOVERY_OTHER);
    }
}