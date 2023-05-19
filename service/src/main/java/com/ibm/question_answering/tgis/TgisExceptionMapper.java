package com.ibm.question_answering.tgis;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class TgisExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    public final static String ERROR_TGIS_PREFIX = "TGIS error: ";
    public final static String ERROR_TGIS_500 = ERROR_TGIS_PREFIX + "The remote service responded with HTTP 500";
    public final static String ERROR_TGIS_405 = ERROR_TGIS_PREFIX + "The remote service responded with HTTP 405";
    public final static String ERROR_TGIS_401 = ERROR_TGIS_PREFIX + "The remote service responded with HTTP 401";
    public final static String ERROR_TGIS_OTHER = ERROR_TGIS_PREFIX + "Other";
    public final static String ERROR_TGIS_UNEXPECTED = ERROR_TGIS_PREFIX + "Unexpected";

	@Override
    public RuntimeException toThrowable(Response response) {        
        
        if (response.getStatus() == 500) {            
            System.err.println(ERROR_TGIS_500);
            throw new RuntimeException(ERROR_TGIS_500);
        }
        if (response.getStatus() == 405) {
            System.err.println(ERROR_TGIS_405);
            throw new RuntimeException(ERROR_TGIS_405);
        }
        if (response.getStatus() == 401) {
            System.out.println(ERROR_TGIS_401);
            throw new RuntimeException(ERROR_TGIS_401);
        }
        System.err.println(ERROR_TGIS_OTHER);
        throw new RuntimeException(ERROR_TGIS_OTHER);
    }
}