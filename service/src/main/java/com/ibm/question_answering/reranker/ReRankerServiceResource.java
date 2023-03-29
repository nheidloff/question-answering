package com.ibm.question_answering.reranker;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
public class ReRankerServiceResource {

    @RestClient
    ReRankerService reRankerService;

    @POST
    @Path("/RerankRequest")
    public DocumentScore[][] ask(Input input) {
        return reRankerService.ask(input);
    }
}
