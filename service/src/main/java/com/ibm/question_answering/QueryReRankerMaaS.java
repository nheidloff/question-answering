package com.ibm.question_answering;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.discovery.AskDiscoveryService;
import com.ibm.question_answering.file.DocumentScoreUrl;
import com.ibm.question_answering.file.DocumentsReader;
import com.ibm.question_answering.maas.AskModelAsAService;
import com.ibm.question_answering.primeqa.AnswerDocument;
import com.ibm.question_answering.prompts.QuestionAnswering;
import com.ibm.question_answering.prompts.Summarization;
import com.ibm.question_answering.reranker.AskReRankerService;
import com.ibm.question_answering.reranker.DocumentScore;

@ApplicationScoped
public class QueryReRankerMaaS {
    
    @Inject
    DocumentsReader documentsReader;

    @Inject
    AskDiscoveryService askDiscoveryService;

    @Inject
    AskReRankerService askReRankerService;

    @Inject
    AskModelAsAService askMaaS;

    @Inject
    QueryPrimeAndMaaS queryPrimeAndMaaS;

    @Inject
    QuestionAnswering questionAnswering;

    @Inject
    Summarization summarization;

    @Inject
    Metrics metrics;

    public Answer query(String query) {     

        // 1. Read documents from file    
        List<com.ibm.question_answering.file.Document> documents = documentsReader.read();
        DocumentScoreUrl[] documentScoreUrls = documentsReader.getDocumentScoreUrls(documents);  
        DocumentScore[] reRankerInput = documentsReader.getReRankerInput(documentScoreUrls);  
        
        // 2. ReRanker
        DocumentScore[][] documentsAndScoresArray = askReRankerService.executeAndReturnRawAnswer(query, reRankerInput);
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
        AnswerDocument[] answerDocuments = convertToAnswerDocuments(documentsAndScores, documentScoreUrls);
        if ((answerDocuments == null) || (answerDocuments.length < 1)) {
            return MockAnswers.getEmptyAnswer();
        }
        Answer output = queryPrimeAndMaaS.queryMaaS(answerDocuments, query);
        metrics.maaSStopped(output);

        return output;
    }

    public AnswerDocument[] convertToAnswerDocuments(DocumentScore[] documentsAndScoresOutput, DocumentScoreUrl[] documentScoreUrlsInput) {
        AnswerDocument[] output = null;
        if (documentsAndScoresOutput != null) {
            output = new AnswerDocument[documentsAndScoresOutput.length];
            for (int index = 0; index < documentsAndScoresOutput.length; index++) {
                AnswerDocument answerDocument = new AnswerDocument();
                com.ibm.question_answering.primeqa.Document document = new com.ibm.question_answering.primeqa.Document();
                document.score = documentsAndScoresOutput[index].score;
                document.text = documentsAndScoresOutput[index].document.text;
                document.title = documentsAndScoresOutput[index].document.title;
                document.document_id = documentsAndScoresOutput[index].document.document_id;
                document.url = documentScoreUrlsInput[index].url;
                com.ibm.question_answering.primeqa.Answer answer = new com.ibm.question_answering.primeqa.Answer();
                answer.text = document.text; 
                answerDocument.answer = answer;
                answerDocument.document = document;
                output[index] = answerDocument;
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
