package com.ibm.question_answering.elasticsearch;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@Produces("application/json")
@Consumes("application/json")
@RegisterRestClient(configKey = "elasticsearch")
@RegisterClientHeaders(CustomHeaderFactory.class)
@ApplicationScoped
@RegisterProvider(ElasticExceptionMapper.class)
public interface ElasticService {

    @POST
    public Response search(Input input);
}