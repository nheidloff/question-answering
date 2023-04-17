package com.ibm.question_answering.prompts;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.question_answering.Metrics;
import com.ibm.question_answering.primeqa.AnswerDocument;

@ApplicationScoped
public class QuestionAnswering {

    @ConfigProperty(name = "EXPERIMENT_LLM_PROMPT") 
    Optional<String> promptOptionalString;

    @Inject
    Metrics metrics;

    public final static String CONTEXT = "<<CONTEXT>>";
    public final static String QUESTION = "<<QUESTION>>";

/* 
    public final String template = """
Answer the following question after reading the text.

Text: <<CONTEXT>>

Question: <<QUESTION>>

Answer: """;
*/

    public final String template = """
<<CONTEXT>>

User: <<QUESTION>>
Agent:""";


    public String getPrompt(String query, String context) {
        String output = template;
        String promptVar = "";
        if (promptOptionalString.isPresent()) {
            promptVar = promptOptionalString.get();
        }
        if (!promptVar.equalsIgnoreCase("")) {
            output = promptVar;
            output = output.replaceAll("\\\\n", System.getProperty("line.separator"));
        }
        metrics.setPromptTemplate(output);
        
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
                    //primeQADocuments[index].document.title + ". " + 
                    primeQADocuments[index].document.text; 
                if (index < amountDocuments - 1) {
                    context = context + System.getProperty("line.separator") + System.getProperty("line.separator");
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
