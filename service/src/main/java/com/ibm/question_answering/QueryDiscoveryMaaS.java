package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.api.Result;
import com.ibm.question_answering.discovery.AskDiscoveryService;
import com.ibm.question_answering.discovery.RelevantOutput;
import com.ibm.question_answering.maas.AskModelAsAService;
import com.ibm.question_answering.primeqa.AnswerDocument;
import com.ibm.question_answering.prompts.QuestionAnswering;
import com.ibm.question_answering.reranker.Document;
import com.ibm.question_answering.reranker.DocumentScore;

@ApplicationScoped
public class QueryDiscoveryMaaS {
      
    @Inject
    QueryDiscoveryReRankerMaaS queryDiscoveryReRankerMaaS;

    @Inject
    AskDiscoveryService askDiscoveryService;

    @Inject
    AskModelAsAService askMaaS;

    @Inject
    QueryPrimeAndMaaS queryPrimeAndMaaS;

    @Inject
    QuestionAnswering questionAnswering;

    @Inject
    Metrics metrics;

    public Answer query(String query) {
        
        // 1. Discovery
        com.ibm.question_answering.api.Answer discoveryAnswer = askDiscoveryService.ask(query);   
        if ((discoveryAnswer == null) || (discoveryAnswer.matching_results < 1)) {
            return MockAnswers.getEmptyAnswer();
        }
        for (int index = 0; index < discoveryAnswer.results.size(); index++) {
            discoveryAnswer.results.get(index).document_id = discoveryAnswer.results.get(index).chunckid;
        }
        
        // 2. MaaS
        DocumentScore[] documentsAndScores = convert(discoveryAnswer);
        AnswerDocument[] answerDocuments = queryDiscoveryReRankerMaaS.convertToAnswerDocuments(documentsAndScores, discoveryAnswer, documentsAndScores.length);
        if ((answerDocuments == null) || (answerDocuments.length < 1)) {
            return MockAnswers.getEmptyAnswer();
        }

        Answer output = queryPrimeAndMaaS.queryMaaS(answerDocuments, query);
        String answerAsText = output.results.get(0).text.text[0];
        answerAsText = queryDiscoveryReRankerMaaS.removeEverythingAfterLastDot(answerAsText);
        String[] text = new String[1];
        text[0] = answerAsText;
        output.results.get(0).text.text = text;
        metrics.maaSStopped(output);

        return output;
    }

    public DocumentScore[] convert(com.ibm.question_answering.api.Answer discoveryAnswer) {
        DocumentScore[] output = new DocumentScore[0];
        if (discoveryAnswer != null) {
            if (discoveryAnswer.results != null) {
                if (discoveryAnswer.results.size() > 0) {
                    output = new DocumentScore[discoveryAnswer.results.size()];
                    for (int index = 0; index < discoveryAnswer.results.size(); index++) {
                        Result discoveryResult = discoveryAnswer.results.get(index);
                        Document document = new Document();
                        document.text = RelevantOutput.getDiscoveryResultAsText(discoveryAnswer, index);
                        document.document_id = discoveryResult.document_id;
                        document.title = discoveryResult.title;
                        DocumentScore documentScore = new DocumentScore(document, discoveryResult.result_metadata.confidence);
                        output[index] = documentScore;
                    }
                }
            }
        }
        return output;
    }
}
