package com.ibm.question_answering.maas;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
public class ModelAsAServiceResource {
    final String MAAS_URL_NOT_SET = "NOT_SET";

    @RestClient
    ModelAsAService modelAsAService;

    @POST
    @Path("/ask")
    public Answer ask(Input input) {
        return modelAsAService.ask(input);
    }
}
