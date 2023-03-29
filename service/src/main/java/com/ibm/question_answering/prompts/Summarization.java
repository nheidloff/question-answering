package com.ibm.question_answering.prompts;

import javax.enterprise.context.ApplicationScoped;
import com.ibm.question_answering.primeqa.AnswerDocument;

@ApplicationScoped
public class Summarization {

    public final static String CONTEXT = "<<CONTEXT>>";

    public final String template = """
<<CONTEXT>>
        
Explain the above in one sentence: """;

    public String getPrompt(String context) {
        String output = template;
        output = output.replace(CONTEXT, context);
        return output;
    }

    public String getPrompt(AnswerDocument[] primeQADocuments, int index) {
        String context = "";
        int amountDocuments = primeQADocuments.length;
        if (index < amountDocuments) {
            context = primeQADocuments[index].document.title + "\n" + primeQADocuments[index].document.text;
         }
        return getPrompt(context);
    }
}
