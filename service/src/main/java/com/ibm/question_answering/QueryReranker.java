package com.ibm.question_answering;

import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ibm.question_answering.reranker.AskReRankerService;
import com.ibm.question_answering.reranker.Document;
import com.ibm.question_answering.reranker.DocumentScore;

@ApplicationScoped
public class QueryReranker {
    
    @Inject
    AskReRankerService askReRankerService;

    public Answer query(String query) {
        DocumentScore[] documentsAndScores = new DocumentScore[2];
        Document document = new Document();
        document.text = MockAnswers.getResultDocument1().text.text[0];
        document.document_id = MockAnswers.getResultDocument1().document_id;
        document.title = MockAnswers.getResultDocument1().title;
        documentsAndScores[0] = new DocumentScore(document, 1.4);

        document = new Document();
        document.text = MockAnswers.getResultDocument2().text.text[0];
        document.document_id = MockAnswers.getResultDocument2().document_id;
        document.title = MockAnswers.getResultDocument2().title;
        documentsAndScores[1] = new DocumentScore(document, 1.4);
        
        DocumentScore[][] documentsAndScoresArray = askReRankerService.executeAndReturnRawAnswer(query, documentsAndScores);
        if (documentsAndScoresArray == null) return MockAnswers.getEmptyAnswer();
        
        documentsAndScores = documentsAndScoresArray[0];
        ArrayList<Result> results = new ArrayList<Result>();
        for (int index = 0; index < documentsAndScores.length; index++) {
            String[] text = new String[1];
            System.out.println("Document:");
            System.out.println(documentsAndScores[index].document.title);
            System.out.println(documentsAndScores[index].score);
            text[0] = documentsAndScores[index].document.text;
            results.add(new Result(documentsAndScores[index].document.document_id, 
                documentsAndScores[index].document.title, 
                text, 
                null, 
                null));
        }
        Answer output = new Answer(false, documentsAndScores.length, results);

        return output;
    }
}