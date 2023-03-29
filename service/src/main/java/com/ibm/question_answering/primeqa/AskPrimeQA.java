package com.ibm.question_answering.primeqa;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.DocumentPassage;
import com.ibm.question_answering.PassageAnswer;
import com.ibm.question_answering.Result;

@ApplicationScoped
public class AskPrimeQA {
    public AskPrimeQA() {}

    @Inject
    PrimeQAResource primeQAResource;
    
    final String DISCOVERY_COLLECTION_ID_NOT_SET = "NOT_SET";
        
    @ConfigProperty(name = "DISCOVERY_COLLECTION_ID", defaultValue = DISCOVERY_COLLECTION_ID_NOT_SET) 
    public String collectionId;

    public com.ibm.question_answering.Answer execute(String query) {       
        AnswerDocument[] response = primeQAResource.ask(createInput(query));

        com.ibm.question_answering.Answer output = new com.ibm.question_answering.Answer(false, 0, null);
        if (response != null) {
            output.matching_results = response.length;
            ArrayList<Result> results = new ArrayList<Result>();
            for (int index = 0; index < response.length; index++) {
                results.add(getAnswerDocument(response[index]));
            }
            output.results = results;
        }

        return output;
    }

    public AnswerDocument[] executeAndReturnRawAnswerDocuments(String query) {
        return primeQAResource.ask(createInput(query));
    }

    private Input createInput(String query) {
        List<Parameter> retrieverParameters = new ArrayList<Parameter>();
        retrieverParameters.add(new Parameter("count", "5"));
        
        List<Parameter> readerParameters = new ArrayList<Parameter>();
        readerParameters.add(new Parameter("model", "PrimeQA/nq_tydi_sq1-reader-xlmr_large-20221110"));
        readerParameters.add(new Parameter("max_num_answers", "1"));
        readerParameters.add(new Parameter("max_answer_length", "50"));
        
        Input input = new Input(collectionId, 
            query, 
            Retriever.RETRIEVER_ID_WATSON_DISCOVERY, retrieverParameters, Retriever.PROVENANCE_WATSON_DISCOVERY,
            Reader.READER_ID_PRIME_QA, readerParameters, Reader.PROVENANCE_PRIME_QA);
        return input;
    }

    public Result getAnswerDocument(AnswerDocument answerDocument) {
        Result output = null;
        if (answerDocument != null) {
            DocumentPassage[] documentPassages = new DocumentPassage[1];
            PassageAnswer[] passageAnswers = new PassageAnswer[1];
            passageAnswers[0] = new PassageAnswer(answerDocument.answer.text, answerDocument.answer.confidence_score);
            documentPassages[0] = new DocumentPassage(answerDocument.answer.text, DocumentPassage.FIELD_TEXT,passageAnswers);
            String text[] = new String[1];
            text[0] = answerDocument.document.text;
            output = new Result(answerDocument.document.document_id, 
                answerDocument.document.title, 
                text,
                answerDocument.document.url,
                documentPassages);
        }
        return output;
    }
}
