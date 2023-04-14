package com.ibm.question_answering.discovery;

import javax.ws.rs.Produces;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@Produces("application/json")
@Consumes("application/json")
@RegisterRestClient(configKey = "discovery")
@RegisterClientHeaders(CustomHeaderFactory.class)
@ApplicationScoped
@RegisterProvider(DiscoveryExceptionMapper.class)
public interface DiscoveryService {
    
    @POST
    com.ibm.question_answering.api.Answer ask(Input input);
}
