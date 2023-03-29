package com.ibm.question_answering.primeqa;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
public class PrimeQAResource {
    final String PRIME_QA_URL_NOT_SET = "NOT_SET";

    @RestClient
    PrimeQAService primeQAService;

    @POST
    @Path("/ask")
    public AnswerDocument[] ask(Input input) {
        return primeQAService.ask(input);
    }
}
