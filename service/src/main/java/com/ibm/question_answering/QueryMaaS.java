package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ibm.question_answering.api.Answer;
import com.ibm.question_answering.maas.AskModelAsAService;

@ApplicationScoped
public class QueryMaaS {

    @Inject
    AskModelAsAService askMaaS;

    public Answer query(String prompt) {  
        Answer answer = askMaaS.execute(prompt);
        String answerAsText = answer.results.get(0).text.text[0];
        String[] text = new String[1];
        text[0] = answerAsText;
        answer.results.get(0).text.text = text;
        return answer;   
    }  
}