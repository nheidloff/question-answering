package com.ibm.question_answering.maas;

import java.util.ArrayList;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.Metrics;
import com.ibm.question_answering.Result;
import com.ibm.question_answering.primeqa.AnswerDocument;
import com.ibm.question_answering.prompts.QuestionAnswering;
import com.ibm.question_answering.proxy.ProxyExceptionMapper;
import com.ibm.question_answering.proxy.ProxyServiceResource;

@ApplicationScoped
public class AskModelAsAService {
    public AskModelAsAService() {}

    @Inject
    Metrics metrics;

    @Inject
    QuestionAnswering questionAnswering;
    
    @Inject
    ModelAsAServiceResource maasResource;

    @Inject
    ProxyServiceResource proxyResource;

    final String PROXY_API_KEY_NOT_SET = "NOT_SET"; 
    @ConfigProperty(name = "PROXY_API_KEY", defaultValue = PROXY_API_KEY_NOT_SET) 
    private String proxyApiKey;
    private boolean useProxy = false;

    final String PROXY_URL_NOT_SET = "NOT_SET"; 
    @ConfigProperty(name = "PROXY_URL", defaultValue = PROXY_URL_NOT_SET) 
    private String proxyUrl;
    final static String ERROR_PROXY_URL_NOT_SET = ProxyExceptionMapper.ERROR_PROXY_PREFIX + "PROXY_URL not defined";

    final String MAAS_URL_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "MAAS_URL", defaultValue = MAAS_URL_NOT_SET) 
    private String url;
    final static String ERROR_MAAS_URL_NOT_SET = MaaSExceptionMapper.ERROR_MAAS_PREFIX + "MAAS_URL not defined";

    final String MAAS_API_KEY_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "MAAS_API_KEY", defaultValue = MAAS_API_KEY_NOT_SET) 
    private String apiKey;
    final static String ERROR_MAAS_API_KEY_NOT_SET = MaaSExceptionMapper.ERROR_MAAS_PREFIX + "MAAS_API_KEY not defined";

    final static String MAAS_LLM_NAME = "google/flan-t5-xxl";
    @ConfigProperty(name = "EXPERIMENT_LLM_NAME") 
    Optional<String> llmNameOptionalString;

    final static int MAAS_LLM_MIN_NEW_TOKENS = 1;
    @ConfigProperty(name = "EXPERIMENT_LLM_MIN_NEW_TOKENS") 
    Optional<String> llmMinNewTokensOptionalString;

    final static int MAAS_LLM_MAX_NEW_TOKENS = 300;
    @ConfigProperty(name = "EXPERIMENT_LLM_MAX_NEW_TOKENS") 
    Optional<String> llmMaxNewTokensOptionalString;

    final static int MAAS_LLM_MAX_INPUT_DOCUMENTS = 3;
    @ConfigProperty(name = "EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS") 
    Optional<String> llmMaxInputDocumentsOptionalString;

    public com.ibm.question_answering.Answer execute(String query, AnswerDocument[] answerDocuments) {        
        int llmMaxInputDocuments = MAAS_LLM_MAX_INPUT_DOCUMENTS;
        if (llmMaxInputDocumentsOptionalString.isPresent()) {
            try {
                llmMaxInputDocuments = Integer.parseInt(llmMaxInputDocumentsOptionalString.get());
            } catch (Exception e) {}
        }
        metrics.setMaaSMaxAmountDocuments(llmMaxInputDocuments);
        if (llmMaxInputDocuments < answerDocuments.length) {
            AnswerDocument[] answerDocumentsOrg = answerDocuments;
            answerDocuments = new AnswerDocument[llmMaxInputDocuments];
            System.arraycopy(answerDocumentsOrg, 0, answerDocuments, 0, llmMaxInputDocuments);
        }
        String prompt = questionAnswering.getPrompt(query, answerDocuments);
        return execute(prompt);
    }

    public com.ibm.question_answering.Answer execute(String prompt) {
        if (!proxyApiKey.equalsIgnoreCase(PROXY_API_KEY_NOT_SET)) {
            useProxy = true;
            if (proxyUrl.equalsIgnoreCase(PROXY_URL_NOT_SET)) {
                System.err.println(ERROR_PROXY_URL_NOT_SET);
                throw new RuntimeException(ERROR_PROXY_URL_NOT_SET);
            }
        }
        if (url.equalsIgnoreCase(MAAS_URL_NOT_SET)) {
            System.err.println(ERROR_MAAS_URL_NOT_SET);
            throw new RuntimeException(ERROR_MAAS_URL_NOT_SET);
        }
        if (apiKey.equalsIgnoreCase(MAAS_API_KEY_NOT_SET)) {
            System.err.println(ERROR_MAAS_API_KEY_NOT_SET);
            throw new RuntimeException(ERROR_MAAS_API_KEY_NOT_SET);
        }
        int llmMinNewTokens = MAAS_LLM_MIN_NEW_TOKENS;
        if (llmMinNewTokensOptionalString.isPresent()) {
            try {
                llmMinNewTokens = Integer.parseInt(llmMinNewTokensOptionalString.get());
            } catch (Exception e) {}
        }
        int llmMaxNewTokens = MAAS_LLM_MAX_NEW_TOKENS;
        if (llmMaxNewTokensOptionalString.isPresent()) {
            try {
                llmMaxNewTokens = Integer.parseInt(llmMaxNewTokensOptionalString.get());
            } catch (Exception e) {}
        }        
        String llmName = MAAS_LLM_NAME;
        if (llmNameOptionalString.isPresent()) {
            llmName = llmNameOptionalString.get();
        }        

        com.ibm.question_answering.maas.Parameters parameters = new com.ibm.question_answering.maas.Parameters();
        parameters.min_new_tokens = llmMinNewTokens;
        parameters.max_new_tokens = llmMaxNewTokens;
        parameters.temperature = 0;
        metrics.maaSStarted(llmMinNewTokens, llmMaxNewTokens, llmName, prompt);
        com.ibm.question_answering.Answer output;
        output = new com.ibm.question_answering.Answer(true, 0, null);
        String[] inputs = new String[1];
        inputs[0] = prompt;

        Answer response;
        if (useProxy == false) {
            response = maasResource.ask(new Input(llmName, inputs, parameters));
        }
        else {
            com.ibm.question_answering.proxy.Input proxyInput = new com.ibm.question_answering.proxy.Input(apiKey, url, 
                new com.ibm.question_answering.maas.Input(llmName, inputs, parameters));
            response = proxyResource.ask(proxyInput);
        }
        
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
