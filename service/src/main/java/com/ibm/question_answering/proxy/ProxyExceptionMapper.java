package com.ibm.question_answering.proxy;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class ProxyExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    public final static String ERROR_PROXY_PREFIX = "Proxy (BAM) error: ";
    public final static String ERROR_PROXY_500 = ERROR_PROXY_PREFIX + "The remote service responded with HTTP 500";
    public final static String ERROR_PROXY_405 = ERROR_PROXY_PREFIX + "The remote service responded with HTTP 405";
    public final static String ERROR_PROXY_401 = ERROR_PROXY_PREFIX + "The remote service responded with HTTP 401";
    public final static String ERROR_PROXY_OTHER = ERROR_PROXY_PREFIX + "Other";
    public final static String ERROR_PROXY_UNEXPECTED = ERROR_PROXY_PREFIX + "Unexpected";

	@Override
    public RuntimeException toThrowable(Response response) {        
        
        if (response.getStatus() == 500) {            
            System.err.println(ERROR_PROXY_500);
            throw new RuntimeException(ERROR_PROXY_500);
        }
        if (response.getStatus() == 405) {
            System.err.println(ERROR_PROXY_405);
            throw new RuntimeException(ERROR_PROXY_405);
        }
        if (response.getStatus() == 401) {
            System.out.println(ERROR_PROXY_401);
            throw new RuntimeException(ERROR_PROXY_401);
        }
        System.err.println(ERROR_PROXY_OTHER);
        throw new RuntimeException(ERROR_PROXY_OTHER);
    }
}