package com.ibm.question_answering.maas;

import java.util.ArrayList;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.Metrics;
import com.ibm.question_answering.api.Result;
import com.ibm.question_answering.primeqa.AnswerDocument;
import com.ibm.question_answering.primeqa.AskPrimeQA;
import com.ibm.question_answering.prompts.QuestionAnswering;
import com.ibm.question_answering.proxy.ProxyExceptionMapper;
import com.ibm.question_answering.proxy.ProxyServiceResource;
import com.ibm.question_answering.tgis.Parameters;
import com.ibm.question_answering.tgis.Request;
import com.ibm.question_answering.tgis.Sampling;
import com.ibm.question_answering.tgis.Stopping;
import com.ibm.question_answering.tgis.TgisServiceResource;

import io.smallrye.mutiny.Multi;

@ApplicationScoped
public class AskModelAsAService {
    public AskModelAsAService() {}

    @Inject
    Metrics metrics;

    @Inject
    AskPrimeQA askPrimeQA;
    
    @Inject
    QuestionAnswering questionAnswering;
    
    @Inject
    ModelAsAServiceResource maasResource;

    @Inject
    TgisServiceResource tgisResource;

    @Inject
    ProxyServiceResource proxyResource;

    final String PROXY_API_KEY_NOT_SET = "NOT_SET"; 
    String proxyApiKey = System.getenv("PROXY_API_KEY");
    private boolean useProxy = false;

    final String PROXY_URL_NOT_SET = "NOT_SET"; 
    private String proxyUrl = System.getenv("PROXY_URL");
    final static String ERROR_PROXY_URL_NOT_SET = ProxyExceptionMapper.ERROR_PROXY_PREFIX + "PROXY_URL not defined";

    String tgisUrl = System.getenv("TGIS");
    private boolean useTegis = false;

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
    String llmName = MAAS_LLM_NAME;

    final static int MAAS_LLM_MIN_NEW_TOKENS = 1;
    @ConfigProperty(name = "EXPERIMENT_LLM_MIN_NEW_TOKENS") 
    Optional<String> llmMinNewTokensOptionalString;
    int llmMinNewTokens = MAAS_LLM_MIN_NEW_TOKENS;

    final static int MAAS_LLM_MAX_NEW_TOKENS = 300;
    @ConfigProperty(name = "EXPERIMENT_LLM_MAX_NEW_TOKENS") 
    Optional<String> llmMaxNewTokensOptionalString;
    int llmMaxNewTokens = MAAS_LLM_MAX_NEW_TOKENS;

    final static int MAAS_LLM_MAX_INPUT_DOCUMENTS = 3;
    @ConfigProperty(name = "EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS") 
    Optional<String> llmMaxInputDocumentsOptionalString;

    final static int MAX_RESULTS = 5;
    @ConfigProperty(name = "MAX_RESULTS") 
    Optional<String> maxResultsOptionalString;
    int maxResults = MAX_RESULTS;

    private AnswerDocument[] limitAnswerDocuments(AnswerDocument[] answerDocuments) {
        if (maxResultsOptionalString.isPresent()) {
            try {
                maxResults = Integer.parseInt(maxResultsOptionalString.get());
            } catch (Exception e) {}
        }  
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
        return answerDocuments;
    }

    private com.ibm.question_answering.api.Answer limitAnswerDocuments(com.ibm.question_answering.api.Answer answer) {        
        if (maxResultsOptionalString.isPresent()) {
            try {
                maxResults = Integer.parseInt(maxResultsOptionalString.get());
            } catch (Exception e) {}
        }  
        int llmMaxInputDocuments = MAAS_LLM_MAX_INPUT_DOCUMENTS;
        if (llmMaxInputDocumentsOptionalString.isPresent()) {
            try {
                llmMaxInputDocuments = Integer.parseInt(llmMaxInputDocumentsOptionalString.get());
            } catch (Exception e) {}
        }
        metrics.setMaaSMaxAmountDocuments(llmMaxInputDocuments);
        if (llmMaxInputDocuments < answer.results.size()) {
            com.ibm.question_answering.api.Answer answerOrg = answer;
            answer = new com.ibm.question_answering.api.Answer(false, llmMaxInputDocuments, null);
            answer.results = new ArrayList<Result>();
            for (int index = 0; index < llmMaxInputDocuments; index++) {
                answer.results.add(answerOrg.results.get(index));
            }
        }
        return answer;
    }

    public com.ibm.question_answering.api.Answer execute(String query, AnswerDocument[] answerDocuments) {      
        answerDocuments = this.limitAnswerDocuments(answerDocuments);
        String prompt = questionAnswering.getPrompt(query, answerDocuments);
        com.ibm.question_answering.api.Answer output = execute(prompt);
        output = cleanUpAnswer(output, answerDocuments);
        return output;
    }

    public com.ibm.question_answering.api.Answer execute(String query, com.ibm.question_answering.api.Answer answer) {      
        answer = this.limitAnswerDocuments(answer);
        String prompt = questionAnswering.getPrompt(query, answer);
        com.ibm.question_answering.api.Answer output = execute(prompt);
        output = cleanUpAnswer(output, answer);
        return output;
    }

    private void readAndCheckEnvironmentVariables() {
        if ((tgisUrl != null) && (!tgisUrl.equals(""))) {
            if (tgisUrl.equalsIgnoreCase("true")) {
                useTegis = true;
            }
        }
        if ((proxyApiKey != null) && (!proxyApiKey.equals(""))) {
            if (!proxyApiKey.equalsIgnoreCase(PROXY_API_KEY_NOT_SET)) {
                useProxy = true;
                if ((proxyUrl == null) || (proxyUrl.equals("")) || (proxyUrl.equalsIgnoreCase(PROXY_URL_NOT_SET))) {
                    System.err.println(ERROR_PROXY_URL_NOT_SET);
                    throw new RuntimeException(ERROR_PROXY_URL_NOT_SET);
                }
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
        if (llmMinNewTokensOptionalString.isPresent()) {
            try {
                llmMinNewTokens = Integer.parseInt(llmMinNewTokensOptionalString.get());
            } catch (Exception e) {}
        }
        if (llmMaxNewTokensOptionalString.isPresent()) {
            try {
                llmMaxNewTokens = Integer.parseInt(llmMaxNewTokensOptionalString.get());
            } catch (Exception e) {}
        }        
        if (llmNameOptionalString.isPresent()) {
            llmName = llmNameOptionalString.get();
        }        
    }

    public Multi<com.ibm.question_answering.maas.Answer> executeAsStream(String query, AnswerDocument[] answerDocuments) {
        answerDocuments = this.limitAnswerDocuments(answerDocuments);
        String prompt = questionAnswering.getPrompt(query, answerDocuments);
        return executeAsStream(prompt);
    }

    public Multi<com.ibm.question_answering.maas.Answer> executeAsStream(String prompt) {
        this.readAndCheckEnvironmentVariables();
        metrics.maaSStarted(llmMinNewTokens, llmMaxNewTokens, llmName, prompt);
        com.ibm.question_answering.maas.Parameters parameters = getParameters();
        parameters.stream = true;

        Multi<Answer> response = null;
        // TODO throw error for tgis
        if (useProxy == false) {
            response = maasResource.askAsStream(new Input(llmName, getInputs(prompt), parameters));
        }
        else {
           throw new RuntimeException(ProxyExceptionMapper.ERROR_PROXY_PREFIX + ProxyExceptionMapper.ERROR_PROXY_PREFIX);
        }    
        return response;
    }

    private com.ibm.question_answering.maas.Parameters getParameters() {
        com.ibm.question_answering.maas.Parameters parameters = new com.ibm.question_answering.maas.Parameters();
        parameters.min_new_tokens = llmMinNewTokens;
        parameters.max_new_tokens = llmMaxNewTokens;
        parameters.temperature = 0;
        return parameters;
    }

    private String[] getInputs(String prompt) {
        String[] inputs = new String[1];
        inputs[0] = prompt;
        return inputs;
    }

    public com.ibm.question_answering.api.Answer execute(String prompt) {
        this.readAndCheckEnvironmentVariables();
        metrics.maaSStarted(llmMinNewTokens, llmMaxNewTokens, llmName, prompt);       
        
        Answer response;
        if (useTegis == true) {
            com.ibm.question_answering.tgis.Input input = new com.ibm.question_answering.tgis.Input();
            input.modelId = llmName;
            input.requests = new Request[1];
            input.requests[0] = new Request();
            input.requests[0].text = prompt;
            input.params = new Parameters();
            input.params.sampling = new Sampling();
            input.params.stopping = new Stopping();
            input.params.stopping.minNewTokens = llmMinNewTokens;
            input.params.stopping.maxNewTokens = llmMaxNewTokens;
            response = tgisResource.ask(input);
        }
        else {
            if (useProxy == false) {
                response = maasResource.ask(new Input(llmName, getInputs(prompt), getParameters()));
            }
            else {
                com.ibm.question_answering.proxy.Input proxyInput = new com.ibm.question_answering.proxy.Input(apiKey, url, 
                    new com.ibm.question_answering.maas.Input(llmName, getInputs(prompt), getParameters()));
                response = proxyResource.ask(proxyInput);
            }  
        }    
        return convertToAPIAnswer(response);
    }

    private com.ibm.question_answering.api.Answer convertToAPIAnswer(Answer response) {
        com.ibm.question_answering.api.Answer output;
        output = new com.ibm.question_answering.api.Answer(true, 0, null);

        if (response != null) {
            if (response.results.length > 0) {
                String generatedText = getGeneratedText(response);
                output.matching_results = 1;
                ArrayList<Result> results = new ArrayList<Result>();
                String text[] = new String[1];
                text[0] = generatedText;
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

    private String getGeneratedText(Answer response) {
        String output = "";
        String generatedText = "";
        if (response != null) {
            if (response.results.length > 0) {
                generatedText = response.results[0].generated_text;

                // special case
                String EVIDENCE_MARKER1 = "; evidence:";
                String EVIDENCE_MARKER2 = ". evidence:";
                String EVIDENCE_MARKER3 = "? evidence:";
                String RESPONSE_MARKER = "response: ";
                if (generatedText.contains(EVIDENCE_MARKER1)) {
                    generatedText = generatedText.substring(RESPONSE_MARKER.length(), generatedText.indexOf(EVIDENCE_MARKER1));
                    //generatedText = generatedText.substring(generatedText.indexOf(EVIDENCE_MARKER1) + EVIDENCE_MARKER1.length(), generatedText.length()).trim();
                }
                if (generatedText.contains(EVIDENCE_MARKER2)) {
                    generatedText = generatedText.substring(0, generatedText.indexOf(EVIDENCE_MARKER2) + 1);
                    //generatedText = generatedText.substring(generatedText.indexOf(EVIDENCE_MARKER2) + EVIDENCE_MARKER2.length(), generatedText.length()).trim();
                }
                if (generatedText.contains(EVIDENCE_MARKER3)) {
                    generatedText = generatedText.substring(0, generatedText.indexOf(EVIDENCE_MARKER3) + 1);
                    //generatedText = generatedText.substring(generatedText.indexOf(EVIDENCE_MARKER3) + EVIDENCE_MARKER3.length(), generatedText.length()).trim();
                }
            }
        }
        output = generatedText;

        // another special case
        String iDontKnowAnswer = "I cannot find an answer to your question.";
        if (output.startsWith("I do not have information regarding")) {
            output = iDontKnowAnswer;
        }
        if (output.startsWith("I don't know")) {
            output = iDontKnowAnswer;
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
    
    public com.ibm.question_answering.api.Answer cleanUpAnswer(com.ibm.question_answering.api.Answer maasAnswer, com.ibm.question_answering.api.Answer searchAnswer) {
        com.ibm.question_answering.api.Answer output = maasAnswer;
        
        if (searchAnswer != null) {
            output.matching_results = searchAnswer.results.size();
            ArrayList<Result> results = new ArrayList<Result>();
            results.add(output.results.get(0));
            for (int index = 0; index < searchAnswer.results.size(); index++) {
                results.add(searchAnswer.results.get(index));
            }
            output.results = results;
        }

        int results = output.results.size();
        if (maxResults + 1 < results) {
            for (int index = results - 1; index > maxResults; index--) {
                output.results.remove(index);
            }
        }
        
        for (int index = 0; index < output.results.size(); index++) {
            if (output.results.get(index).document_passages != null) {
                output.results.get(index).document_passages =  null;
            }
        }
        return output;
    }

    public com.ibm.question_answering.api.Answer cleanUpAnswer(com.ibm.question_answering.api.Answer output, AnswerDocument[] answerDocuments) {
        if (answerDocuments != null) {
            output.matching_results = answerDocuments.length;
            ArrayList<Result> results = new ArrayList<Result>();
            results.add(output.results.get(0));
            for (int index = 0; index < answerDocuments.length; index++) {
                results.add(askPrimeQA.getAnswerDocument(answerDocuments[index]));
            }
            output.results = results;
        }

        int results = output.results.size();
        if (maxResults + 1 < results) {
            for (int index = results - 1; index > maxResults; index--) {
                output.results.remove(index);
            }
        }
        
        for (int index = 0; index < output.results.size(); index++) {
            if (output.results.get(index).document_passages != null) {
                output.results.get(index).document_passages =  null;
                /*
                int countPassages = output.results.get(index).document_passages.length;
                if (countPassages > 0) {
                    String textRead = output.results.get(index).document_passages[0].passage_text;
                    DocumentPassage[] documentPassages = new DocumentPassage[1];
                    PassageAnswer[] passageAnswers = new PassageAnswer[1];
                    passageAnswers[0] = new PassageAnswer(textRead, 0);
                    passageAnswers[0].field = PassageAnswer.FIELD_SUMMARY;
                    documentPassages[0] = new DocumentPassage("<em>IBM</em> <em>acquires</em> <em>Red</em> <em>Hat</em>", DocumentPassage.FIELD_TEXT, passageAnswers);
                    String text[] = new String[1];
                    text[0] = textRead;
                    output.results.get(index).document_passages = documentPassages;                    
                } 
                */
            }
        }

        /*
        String answerAsText = output.results.get(0).text.text[0];
        answerAsText = removeEverythingAfterLastDot(answerAsText);
        String[] text = new String[1];
        text[0] = answerAsText;
        output.results.get(0).text.text = text;
        */

        return output;
    }
}
