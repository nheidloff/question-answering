package com.ibm.question_answering;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.maas.AskModelAsAService;
import com.ibm.question_answering.maas.Input;
import com.ibm.question_answering.maas.Parameters;

@ApplicationScoped
public class QueryMaaS {
    
    @Inject
    Metrics metrics;

    @Inject
    AskModelAsAService askMaaS;

    @ConfigProperty(name = "experiment.llm-name") 
    Optional<String> modelNameOptional;

    @ConfigProperty(name = "experiment.llm-min-new-tokens") 
    Optional<String> minTokensOptionalString;

    @ConfigProperty(name = "experiment.llm-max-new-tokens") 
    Optional<String> maxTokensOptionalString;

    public Answer query(String query) {  
        String modelName = Input.MODEL_DEFAULT;
        if (modelNameOptional.isPresent()) {
            modelName = modelNameOptional.get();
        }
        int minTokens = Parameters.DEFAULT_MIN_NEW_TOKENS;
        if (minTokensOptionalString.isPresent()) {
            try {
                minTokens = Integer.parseInt(minTokensOptionalString.get());
            } catch (Exception e) {}
        }
        int maxTokens = Parameters.DEFAULT_MAX_NEW_TOKENS;
        if (maxTokensOptionalString.isPresent()) {
            try {
                maxTokens = Integer.parseInt(maxTokensOptionalString.get());
            } catch (Exception e) {}
        }

        com.ibm.question_answering.maas.Parameters parameters = new com.ibm.question_answering.maas.Parameters();
        parameters.min_new_tokens = minTokens;
        parameters.max_new_tokens = maxTokens;
        parameters.temperature = 0;
        Answer answer = askMaaS.execute(query, modelName, parameters);
        String answerAsText = answer.results.get(0).text.text[0];
        answerAsText = removeEverythingAfterLastDot(answerAsText);
        String[] text = new String[1];
        text[0] = answerAsText;
        answer.results.get(0).text.text = text;
        return answer;   
    }

    // Input: "In the Purchasing Details tab, you can find detailed purchasing spend information. Question:"
    // Output: "In the Purchasing Details tab, you can find detailed purchasing spend information."
    public String removeEverythingAfterLastDot(String answer) {
        String output = answer;
        int lastIndexOfDot = answer.lastIndexOf(".");
        if (lastIndexOfDot != -1) {
            output = answer.substring(0, lastIndexOfDot + 1);
            output = output.trim();
        }
        return output;
    }
}
