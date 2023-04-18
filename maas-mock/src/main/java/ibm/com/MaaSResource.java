package ibm.com;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.RestHeader;

@Path("/v1")
public class MaaSResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/generate")
    public Response generate(@RestHeader("Authorization") String apikey,
            @Parameter(description = "query", 
            required = true,
            example = "{\"query\": \"text:When and for how much did IBM acquire Red Hat?\"}") Data data) {
        
        String modelId = data.model_id;
        String[] inputs = data.inputs;
        int temperature = data.parameters.temperature;
        int minNewTokens = data.parameters.min_new_tokens;
        int maxNewTokens = data.parameters.max_new_tokens;
        
        //System.out.println("apikey: " + apikey);
        //System.out.println("model_id: " + modelId);
        //System.out.println("inputs[0]: " + inputs[0]);
        //System.out.println("temperature: " + temperature);
        //System.out.println("minNewTokens: " + minNewTokens);
        //System.out.println("maxNewTokens: " + maxNewTokens);

        Response response = new Response();
        response.model_id = modelId;
        Result[] results = new Result[1];
        response.results = results;

        if (inputs[0].contains("Complete")) {
            results[0] = getResultComplete();
        }
        else if (inputs[0].contains("acquire")) {
            results[0] = getResultAquisition();
        }
        else if (inputs[0].contains("Watson")) {
            results[0] = getResultWatsonNLP();
        }    
        else {
            Result result = new Result();
            result.generated_text = "";
            result.input_token_count = 10;
            result.generated_token_count = 0;
            results[0] = result;
        }    

        return response;
    }

    Result getResultComplete() {
        Result result = new Result();
        result.generated_text = " are strong";
        result.input_token_count = 10;
        result.generated_token_count = 4;
        return result;
    }

    Result getResultAquisition() {
        Result result = new Result();
        result.generated_text = "IBM has acquired Red Hat for $34 billion in October 2018.";
        result.input_token_count = 500;
        result.generated_token_count = 20;
        return result;        
    }

    Result getResultWatsonNLP() {
        Result result = new Result();
        result.generated_text = "Watson NLP can run as containers on multiple clouds via Kubernetes and OpenShift.";
        result.input_token_count = 500;
        result.generated_token_count = 30;
        return result;        
    }
}