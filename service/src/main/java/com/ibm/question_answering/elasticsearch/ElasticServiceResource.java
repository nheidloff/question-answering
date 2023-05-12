package com.ibm.question_answering.elasticsearch;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@RegisterProvider(ElasticExceptionMapper.class)
public class ElasticServiceResource {

    @Inject
    @RestClient
    ElasticService elasticService;

    @POST
    @Path("/_search")
    public Response search(Input input) {
        return elasticService.search(input);
    }
}