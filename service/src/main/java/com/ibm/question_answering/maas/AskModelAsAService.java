package com.ibm.question_answering.maas;

import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.Metrics;
import com.ibm.question_answering.Result;

@ApplicationScoped
public class AskModelAsAService {
    public AskModelAsAService() {}

    @Inject
    Metrics metrics;
    
    @Inject
    ModelAsAServiceResource maasResource;

    final String MAAS_API_KEY_NOT_SET = "NOT_SET";
        
    @ConfigProperty(name = "MAAS_API_KEY", defaultValue = MAAS_API_KEY_NOT_SET) 
    public String apiKey;

    public com.ibm.question_answering.Answer execute(String query, String modelId, Parameters parameters) {
        String[] inputs = new String[1];
        inputs[0] = query;
        return executeQuery(inputs, modelId, parameters);
    }

    public com.ibm.question_answering.Answer execute(String[] queries, String modelId, Parameters parameters) {
        return executeQuery(queries, modelId, parameters);
    }

    private com.ibm.question_answering.Answer executeQuery(String[] queries, String modelId, Parameters parameters) {
        com.ibm.question_answering.Answer output;
        output = new com.ibm.question_answering.Answer(true, 0, null);

        Answer response = maasResource.ask(new Input(modelId, queries, parameters));
        if (response != null) {
            if (response.results.length > 0) {
                output.matching_results = 1;
                ArrayList<Result> results = new ArrayList<Result>();
                String text[] = new String[1];
                text[0] = response.results[0].generated_text;
                results.add(new Result(Result.TITLE_ONE_ANSWER, 
                    Result.TITLE_ONE_ANSWER,
                    text,
                    null,
                    null));
                output.results = results;
            }
        }
        return output;
    }

    public com.ibm.question_answering.maas.Answer executeAndReturnRawAnswer(String query, String modelId, Parameters parameters) {
        String[] inputs = new String[1];
        inputs[0] = query;
        return maasResource.ask(new Input(modelId, inputs, parameters));        
    }

    public com.ibm.question_answering.maas.Answer executeAndReturnRawAnswer(String[] queries, String modelId, Parameters parameters) {
        return maasResource.ask(new Input(modelId, queries, parameters));        
    }
}
