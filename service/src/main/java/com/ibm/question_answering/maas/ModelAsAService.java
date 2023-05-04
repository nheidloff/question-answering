package com.ibm.question_answering.maas;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestStreamElementType;
import io.smallrye.mutiny.Multi;

@Path("")
@Produces("application/json")
@Consumes("application/json")
@RegisterRestClient(configKey = "maas")
@RegisterClientHeaders(CustomHeaderFactory.class)
@ApplicationScoped
@RegisterProvider(MaaSExceptionMapper.class)
public interface ModelAsAService {
    
    @POST
    com.ibm.question_answering.maas.Answer ask(Input input);

    @POST
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    Multi<String> askAsStream(Input input);
}
