package com.ibm.question_answering.maas;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;
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

    public Multi<Answer> askAsStream(Input input) {
        Multi<Answer> stream = modelAsAService.askAsStream(input)
            .onItem().transform((item) -> {
                ObjectMapper objectMapper = new ObjectMapper();
                Answer answer = null;
                try {
                    answer = objectMapper.readValue(item, Answer.class);
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return answer;
            });

        return stream;
        }
}
