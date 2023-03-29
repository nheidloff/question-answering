package com.ibm.question_answering.discovery;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
public class DiscoveryServiceResource {

    @RestClient
    DiscoveryService discoverService;

    @POST
    @Path("/instances")
    public com.ibm.question_answering.Answer ask(Input input) {
        return discoverService.ask(input);
    }
}
