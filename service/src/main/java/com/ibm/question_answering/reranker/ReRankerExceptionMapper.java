package com.ibm.question_answering.reranker;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class ReRankerExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    public final static String ERROR_RERANKER_PREFIX = "Re-Ranker error: ";
    public final static String ERROR_RERANKER_500 = ERROR_RERANKER_PREFIX + "The remote service responded with HTTP 500";
    public final static String ERROR_RERANKER_405 = ERROR_RERANKER_PREFIX + "The remote service responded with HTTP 405";
    public final static String ERROR_RERANKER_401 = ERROR_RERANKER_PREFIX + "The remote service responded with HTTP 401";
    public final static String ERROR_RERANKER_OTHER = ERROR_RERANKER_PREFIX + "Other";
    public final static String ERROR_RERANKER_UNEXPECTED = ERROR_RERANKER_PREFIX + "Unexpected";

	@Override
    public RuntimeException toThrowable(Response response) {        
        
        if (response.getStatus() == 500) {            
            System.err.println(ERROR_RERANKER_500);
            throw new RuntimeException(ERROR_RERANKER_500);
        }
        if (response.getStatus() == 405) {
            System.err.println(ERROR_RERANKER_405);
            throw new RuntimeException(ERROR_RERANKER_405);
        }
        if (response.getStatus() == 401) {
            System.out.println(ERROR_RERANKER_401);
            throw new RuntimeException(ERROR_RERANKER_401);
        }
        System.err.println(ERROR_RERANKER_OTHER);
        throw new RuntimeException(ERROR_RERANKER_OTHER);
    }
}