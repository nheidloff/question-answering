package com.ibm.question_answering.reranker;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class ExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

	@Override
    public RuntimeException toThrowable(Response response) {
        System.err.println(response.toString());
        if (response.getStatus() == 500) {
            System.err.println(response);
            throw new RuntimeException("The remote service responded with HTTP 500");
        }
        if (response.getStatus() == 405) {
            System.err.println(response);
            throw new RuntimeException("The remote service responded with HTTP 405");
        }
        if (response.getStatus() == 401) {
            System.err.println(response);
            throw new RuntimeException("The remote service responded with HTTP 401");
        }
        return null;
    }
}