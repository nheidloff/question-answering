package com.ibm.question_answering.reranker;

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
@RegisterRestClient(configKey = "reranker")
@ApplicationScoped
@RegisterProvider(ReRankerExceptionMapper.class)
public interface ReRankerService {
    
    @POST
    DocumentScore[][] ask(Input input);
}
