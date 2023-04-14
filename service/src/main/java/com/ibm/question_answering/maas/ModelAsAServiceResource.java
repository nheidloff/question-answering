package com.ibm.question_answering.maas;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
@RegisterProvider(MaaSExceptionMapper.class)
public class ModelAsAServiceResource {

    @Inject
    @RestClient
    ModelAsAService modelAsAService;

    @POST
    @Path("/ask")
    public Answer ask(Input input) {
        return modelAsAService.ask(input);
    }
}
