package com.ibm.question_answering.tgis;

import javax.ws.rs.Produces;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@Produces("application/json")
@Consumes("application/json")
@RegisterRestClient(configKey = "maas")
@ApplicationScoped
@RegisterProvider(TgisExceptionMapper.class)
public interface TgisService {
    
    @POST
    com.ibm.question_answering.tgis.Answer ask(Input input);
}
