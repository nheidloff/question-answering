package com.ibm.question_answering.reranker2;

import javax.ws.rs.Produces;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import com.ibm.question_answering.reranker.ReRankerExceptionMapper;


@Path("")
@Produces("application/json")
@Consumes("application/json")
@RegisterRestClient(configKey = "reranker")
@RegisterClientHeaders(CustomHeaderFactory.class)
@ApplicationScoped
@RegisterProvider(ReRankerExceptionMapper.class)
public interface ReRankerService {
    
    @POST
    Response ask(Input input);
}
