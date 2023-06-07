package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.elasticsearch.AskElasticService;
import com.ibm.question_answering.maas.AskModelAsAService;
import com.ibm.question_answering.primeqa.AnswerDocument;
import com.ibm.question_answering.reranker.AskReRankerService;
import com.ibm.question_answering.reranker.DocumentScore;

@ApplicationScoped
public class QueryElasticReRankerMaaS {

    @Inject
    AskElasticService askElastic;

    @Inject
    AskReRankerService askReRankerService;

    @Inject
    AskModelAsAService askMaaS;
    
    @Inject
    Metrics metrics;

    public Answer query(String query) { 

        // 1. Elastic
        com.ibm.question_answering.api.Answer answer = askElastic.search(query);  
        if ((answer == null) || (answer.matching_results < 1)) {
            return MockAnswers.getEmptyAnswer();
        }

        // 2. ReRanker
        int inputReRankerAmountDocuments = answer.results.size();
        DocumentScore[] documentsAndScoresInput = new DocumentScore[inputReRankerAmountDocuments];
        for (int index = 0; index < inputReRankerAmountDocuments; index++) {
            com.ibm.question_answering.reranker.Document document = new com.ibm.question_answering.reranker.Document();
            document.text = getResultAsText(answer, index);
            //document.document_id = answer.results.get(index).document_id;
            document.document_id = answer.results.get(index).url;
            document.title = answer.results.get(index).title;
            double score = 0.0;
            try {
                score = answer.results.get(index).result_metadata.confidence;
            } catch (Exception e) {
            }
            documentsAndScoresInput[index] = new DocumentScore(document, score);            
        }
        DocumentScore[][] documentsAndScoresArray = askReRankerService.executeAndReturnRawAnswer(query, documentsAndScoresInput);
        if ((documentsAndScoresArray == null) || (documentsAndScoresArray.length < 1)) {
            System.err.println(com.ibm.question_answering.reranker.ReRankerExceptionMapper.ERROR_RERANKER_UNEXPECTED);
            throw new RuntimeException(com.ibm.question_answering.reranker.ReRankerExceptionMapper.ERROR_RERANKER_UNEXPECTED);
        }
        DocumentScore[] documentsAndScores = documentsAndScoresArray[0];
        if ((documentsAndScores == null) || (documentsAndScores.length < 1)) {
            System.err.println(com.ibm.question_answering.reranker.ReRankerExceptionMapper.ERROR_RERANKER_UNEXPECTED);
            throw new RuntimeException(com.ibm.question_answering.reranker.ReRankerExceptionMapper.ERROR_RERANKER_UNEXPECTED);
        }
        
        // 3. MaaS
        AnswerDocument[] answerDocuments = QueryDiscoveryReRankerMaaS.convertToAnswerDocuments(documentsAndScores, answer, documentsAndScores.length);
        if ((answerDocuments == null) || (answerDocuments.length < 1)) {
            return MockAnswers.getEmptyAnswer();
        }
        Answer output = askMaaS.execute(query, answerDocuments);
        metrics.maaSStopped(output);

        return output;
    }

    private String getResultAsText(Answer answer, int index) {
        String output = "";
        try {
            for (int indexText = 0; indexText < answer.results.get(index).text.text.length; indexText++) {
                String newLine = answer.results.get(index).text.text[indexText];
                output = output + newLine;
                if ((newLine.endsWith(".")) || (newLine.endsWith("!")) || (newLine.endsWith("?"))) {
                    output = output + " ";
                }
                else {
                    output = output + ". ";
                }
            }
        } catch (Exception e) {
        }
        return output;
    }
}