package com.ibm.question_answering.elasticsearch;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class ElasticExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    public final static String ERROR_ELASTIC_PREFIX = "ElasticSearch error: ";
    public final static String ERROR_ELASTIC_500 = ERROR_ELASTIC_PREFIX + "The remote service responded with HTTP 500";
    public final static String ERROR_ELASTIC_405 = ERROR_ELASTIC_PREFIX + "The remote service responded with HTTP 405";
    public final static String ERROR_ELASTIC_401 = ERROR_ELASTIC_PREFIX + "The remote service responded with HTTP 401";
    public final static String ERROR_ELASTIC_OTHER = ERROR_ELASTIC_PREFIX + "Other";
    public final static String ERROR_ELASTIC_UNEXPECTED = ERROR_ELASTIC_PREFIX + "Unexpected";

	@Override
    public RuntimeException toThrowable(Response response) {        
        
        if (response.getStatus() == 500) {            
            System.err.println(ERROR_ELASTIC_500);
            throw new RuntimeException(ERROR_ELASTIC_500);
        }
        if (response.getStatus() == 405) {
            System.err.println(ERROR_ELASTIC_405);
            throw new RuntimeException(ERROR_ELASTIC_405);
        }
        if (response.getStatus() == 401) {
            System.out.println(ERROR_ELASTIC_401);
            throw new RuntimeException(ERROR_ELASTIC_401);
        }
        System.err.println(ERROR_ELASTIC_OTHER);
        throw new RuntimeException(ERROR_ELASTIC_OTHER);
    }
}