package com.ibm.question_answering.discovery;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
@RegisterProvider(DiscoveryExceptionMapper.class)
public class DiscoveryServiceResource {

    @Inject
    @RestClient
    DiscoveryService discoverService;

    @POST
    @Path("/instances")
    public com.ibm.question_answering.api.Answer ask(Input input) {
        return discoverService.ask(input);
    }
}
