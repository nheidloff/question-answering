package com.ibm.question_answering.proxy;

import javax.ws.rs.Produces;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@Produces("application/json")
@Consumes("application/json")
@RegisterRestClient(configKey = "proxy")
@RegisterClientHeaders(CustomHeaderFactory.class)
@ApplicationScoped
public interface ProxyService {
    
    @POST
    com.ibm.question_answering.maas.Answer ask(Input input);
}
