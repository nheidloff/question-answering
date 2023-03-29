package com.ibm.question_answering.prompts;

import javax.enterprise.context.ApplicationScoped;
import com.ibm.question_answering.primeqa.AnswerDocument;

@ApplicationScoped
public class QuestionAnswering {

    public final static String CONTEXT = "<<CONTEXT>>";
    public final static String QUESTION = "<<QUESTION>>";

    public final String template = """
Answer the following question after reading the text.

Text: <<CONTEXT>>

Question: <<QUESTION>>

Answer: """;

    public String getPrompt(String query, String context) {
        String output = template;
        output = output.replace(CONTEXT, context);
        output = output.replace(QUESTION, query);
        return output;
    }

    public String getPrompt(String query, AnswerDocument[] primeQADocuments) {
        String context = "";
        int amountDocuments = primeQADocuments.length;
        if (primeQADocuments != null) {
            for (int index = 0; index < amountDocuments; index++) {
                context = context + 
                    primeQADocuments[index].document.title + "\n" + 
                    primeQADocuments[index].document.text; 
                if (index < amountDocuments - 1) {
                    context = context + "\n\n";
                }
            }
        }
        return getPrompt(query, context);
    }

    public String getPrompt(String query, String[] documents) {
        String context = "";
        int amountDocuments = documents.length;
        if (documents != null) {
            for (int index = 0; index < amountDocuments; index++) {
                context = context + (documents[index]);
                if (index < amountDocuments - 1) {
                    context = context + "\\n\\n";
                }
            }
        }
        return getPrompt(query, context);
    }
}
