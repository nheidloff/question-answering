package com.ibm.question_answering.tgis;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.ibm.question_answering.maas.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
@RegisterProvider(TgisExceptionMapper.class)
public class TgisServiceResource {

    @Inject
    @RestClient
    TgisService tgisService;

    @POST
    @Path("/ask")
    public com.ibm.question_answering.maas.Answer ask(Input input) {
        com.ibm.question_answering.maas.Answer output = new com.ibm.question_answering.maas.Answer();
        Answer answer = tgisService.ask(input);
        try {
            output.results = new Result[1];
            output.results[0] = new Result();
            output.results[0].generated_text = answer.responses[0].text;
        } catch (Exception e) {
            // TODO
            output.results[0].generated_text = "Error";
        }
        return output;
    }
}
