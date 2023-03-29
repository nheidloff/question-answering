package com.ibm.question_answering;

import com.ibm.question_answering.primeqa.AnswerDocument;
import java.util.ArrayList;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.maas.AskModelAsAService;
import com.ibm.question_answering.maas.Parameters;
import com.ibm.question_answering.primeqa.AskPrimeQA;
import com.ibm.question_answering.prompts.QuestionAnswering;
import com.ibm.question_answering.prompts.Summarization;
import com.ibm.question_answering.proxy.AskProxyService;
import com.ibm.question_answering.maas.Input;

@ApplicationScoped
public class QueryPrimeAndMaaS {
    
    @Inject
    Metrics metrics;

    @Inject
    AskPrimeQA askPrimeQA;

    @Inject
    AskModelAsAService askMaaS;

    @Inject
    AskProxyService askProxy;

    @Inject
    QuestionAnswering questionAnswering;

    @Inject
    Summarization summarization;

    @ConfigProperty(name = "experiment.llm-name") 
    Optional<String> modelNameOptional;

    @ConfigProperty(name = "experiment.llm-min-new-tokens") 
    Optional<String> minTokensOptionalString;

    @ConfigProperty(name = "experiment.llm-max-new-tokens") 
    Optional<String> maxTokensOptionalString;

    public Answer query(String query, boolean proxy, boolean summaries) {
        AnswerDocument[] answerDocuments = askPrimeQA.executeAndReturnRawAnswerDocuments(query);
        return queryMaaS(answerDocuments, query, proxy, summaries);
    }

    public Answer queryMaaS(AnswerDocument[] answerDocuments, String query, boolean proxy, boolean summaries) {
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

        int amountAnswerDocuments = answerDocuments.length;
        String prompt = questionAnswering.getPrompt(query, answerDocuments);
        com.ibm.question_answering.maas.Parameters parameters = new com.ibm.question_answering.maas.Parameters();
        parameters.min_new_tokens = minTokens;
        parameters.max_new_tokens = maxTokens;
        parameters.temperature = 0;
        //System.out.println(prompt);

        metrics.maaSStarted(minTokens, maxTokens, modelName, prompt);
        Answer answer;
        if (proxy == true) {
            answer = askProxy.execute(prompt, modelName, parameters);
        }
        else {
            answer = askMaaS.execute(prompt, modelName, parameters);
        }

        if (summaries == true) {
            String[] summarizationPrompts = new String[amountAnswerDocuments];
            for (int index = 0; index < amountAnswerDocuments; index++) {
                summarizationPrompts[index] = summarization.getPrompt(answerDocuments, index);
            }
            
            parameters.min_new_tokens = minTokens;
            parameters.max_new_tokens = maxTokens;
            parameters.temperature = 0;
            com.ibm.question_answering.maas.Answer summarizationAnswer;
            if (proxy == true) {
                summarizationAnswer = askProxy.executeAndReturnRawAnswer(summarizationPrompts, modelName, parameters);
            }
            else {
                summarizationAnswer = askMaaS.executeAndReturnRawAnswer(summarizationPrompts, modelName, parameters);
            }

            if (answerDocuments != null) {
                answer.matching_results = answerDocuments.length;
                ArrayList<Result> results = new ArrayList<Result>();
                results.add(answer.results.get(0));
                for (int index = 0; index < answerDocuments.length; index++) {
                    Result answerDocument = askPrimeQA.getAnswerDocument(answerDocuments[index]);
                    DocumentPassage[] documentPassagesOrg = answerDocument.document_passages;
                    DocumentPassage[] documentPassagesNew;
                    if (documentPassagesOrg == null) {
                        documentPassagesNew = new DocumentPassage[1];
                    }
                    else {
                        documentPassagesNew = new DocumentPassage[documentPassagesOrg.length + 1];
                        for (int i = 0; i < documentPassagesOrg.length; i++) {
                            documentPassagesNew[i] = documentPassagesOrg[i];
                        }
                    }
                    String summary = summarizationAnswer.results[index].generated_text;
                    DocumentPassage documentPassage = new DocumentPassage(summary, DocumentPassage.FIELD_SUMMARY, null);
                    documentPassagesNew[documentPassagesNew.length - 1] = documentPassage;
                    answerDocument.document_passages = documentPassagesNew;
                    results.add(answerDocument);
                }
                answer.results = results;
            }
        }
        else {
            if (answerDocuments != null) {
                answer.matching_results = answerDocuments.length;
                ArrayList<Result> results = new ArrayList<Result>();
                results.add(answer.results.get(0));
                for (int index = 0; index < answerDocuments.length; index++) {
                    results.add(askPrimeQA.getAnswerDocument(answerDocuments[index]));
                }
                answer.results = results;
            }
        }
        return answer;
    }
}