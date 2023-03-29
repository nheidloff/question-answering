package com.ibm.question_answering;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotAuthorizedException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AnswerResourceUtilities {

    final String QA_API_KEY_NOT_SET = "NOT_SET";
    final String QA_API_KEY_INVALID = "Invalid Question Answering API Key";

    @ConfigProperty(name = "QA_API_KEY", defaultValue = QA_API_KEY_NOT_SET) 
    private String correctAPIKey;

    public void checkAuthorization(String apikey) {
        if (isAuthorizated(getAPIKey(apikey)) == false) {
            throw new NotAuthorizedException(QA_API_KEY_INVALID);
        }
    }

    public String getQuery(Data data) {
        String output = "";
        if (data != null) {
            if (data.getTextQuery() != null) {
                output = data.getTextQuery();
            }
        }
        return output;
    }

    public boolean isAuthorizated(String apikey) {
        boolean output = false;
        if (correctAPIKey.equals("") == false) {
            if (correctAPIKey.equals(QA_API_KEY_NOT_SET) == false) {
                if (apikey.equals(correctAPIKey)) {
                    output = true;
                }
             }
        }
        return output;
    }

    public String getAPIKey(String apikey) {
        String output = null;
        if (apikey != null && apikey.toLowerCase().startsWith("basic")) {
            String base64Encoded = apikey.substring("Basic".length()).trim();
            byte[] decoded = Base64.getDecoder().decode(base64Encoded);
            String credentials = new String(decoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            if (values != null) {
                if (values[0].equals("apikey")) {
                    output = values[1];
                }
            } 
        }
        return output;
    }

    public Answer removeRedundantDocuments(Answer input) {
        Answer output = input;
        int inputResulsSize = input.results.size();
        if (input != null) {
            if (inputResulsSize > 0) {
                String[] urls = new String[inputResulsSize];
                for (int index = 0; index < inputResulsSize; index++) {
                    urls[index] = input.results.get(index).url;
                }
                for (int index = inputResulsSize - 1; index > 0; index--) {
                    String url = input.results.get(index).url;
                    boolean isRedundant = false;
                    for (int i = index - 1; i >= 0; i--) {
                        if (url.equals(urls[i])) isRedundant = true;
                    }
                    if (isRedundant == true) {
                        output.results.remove(index);
                    }
                }
            }
        }
        if (input.retrievalDetails.document_retrieval_strategy.equalsIgnoreCase(RetrievalDetails.DOCUMENT_RETRIEVAL_STATEGY_LLM)) {
            output.matching_results = output.results.size() - 1;
        } 
        else {
            output.matching_results = output.results.size();
        }
        return output;
    }
}