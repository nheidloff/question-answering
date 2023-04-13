package com.ibm.question_answering.discovery;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class ExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    public final static String ERROR_DISCOVERY_PREFIX = "Watson Discovery error: ";
    final static String ERROR_DISCOVERY_500 = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "The remote service responded with HTTP 500";
    final static String ERROR_DISCOVERY_405 = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "The remote service responded with HTTP 405";
    final static String ERROR_DISCOVERY_401 = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "The remote service responded with HTTP 401";
    final static String ERROR_DISCOVERY_OTHER = ExceptionMapper.ERROR_DISCOVERY_PREFIX + "Other";

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