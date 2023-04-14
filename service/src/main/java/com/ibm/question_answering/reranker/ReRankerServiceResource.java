package com.ibm.question_answering.reranker;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
@RegisterProvider(ReRankerExceptionMapper.class)
public class ReRankerServiceResource {

    @Inject
    @RestClient
    ReRankerService reRankerService;

    @POST
    @Path("/RerankRequest")
    public DocumentScore[][] ask(Input input) {
        return reRankerService.ask(input);
    }
}
