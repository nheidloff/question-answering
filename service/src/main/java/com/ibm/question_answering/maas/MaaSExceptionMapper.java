package com.ibm.question_answering.maas;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class MaaSExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    public final static String ERROR_MAAS_PREFIX = "MaaS (BAM) error: ";
    public final static String ERROR_MAAS_500 = ERROR_MAAS_PREFIX + "The remote service responded with HTTP 500";
    public final static String ERROR_MAAS_405 = ERROR_MAAS_PREFIX + "The remote service responded with HTTP 405";
    public final static String ERROR_MAAS_401 = ERROR_MAAS_PREFIX + "The remote service responded with HTTP 401";
    public final static String ERROR_MAAS_OTHER = ERROR_MAAS_PREFIX + "Other";
    public final static String ERROR_MAAS_UNEXPECTED = ERROR_MAAS_PREFIX + "Unexpected";

	@Override
    public RuntimeException toThrowable(Response response) {        
        
        if (response.getStatus() == 500) {            
            System.err.println(ERROR_MAAS_500);
            throw new RuntimeException(ERROR_MAAS_500);
        }
        if (response.getStatus() == 405) {
            System.err.println(ERROR_MAAS_405);
            throw new RuntimeException(ERROR_MAAS_405);
        }
        if (response.getStatus() == 401) {
            System.out.println(ERROR_MAAS_401);
            throw new RuntimeException(ERROR_MAAS_401);
        }
        System.err.println(ERROR_MAAS_OTHER);
        throw new RuntimeException(ERROR_MAAS_OTHER);
    }
}