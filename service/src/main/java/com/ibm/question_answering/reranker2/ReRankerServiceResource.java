package com.ibm.question_answering.reranker2;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import com.ibm.question_answering.reranker.ReRankerExceptionMapper;

@ApplicationScoped
@RegisterProvider(ReRankerExceptionMapper.class)
public class ReRankerServiceResource {

    @Inject
    @RestClient
    ReRankerService reRankerService;

    @POST
    @Path("/rerank")
    public Response ask(Input input) {
        return reRankerService.ask(input);
    }
}
