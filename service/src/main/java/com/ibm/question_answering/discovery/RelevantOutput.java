package com.ibm.question_answering.discovery;

import com.ibm.question_answering.DocumentPassage;
import com.ibm.question_answering.Result;

public class RelevantOutput {

    public static String getDiscoveryResultAsText(com.ibm.question_answering.Answer discoveryAnswer, int index) {
        String output = "";
        if (discoveryAnswer != null) {
            Result discoveryResult = discoveryAnswer.results.get(index);

            // title?
            //output = discoveryResult.title + ".";

            /* classic Discovery query reading text property
            if (discoveryResult.text != null) {
                if (discoveryResult.text.text != null) {                            
                    if (discoveryResult.text.text.length > 0) {
                        for (int i = 0; i < discoveryAnswer.results.get(index).text.text.length; i++) {
                            output = output + " " + discoveryAnswer.results.get(index).text.text[i] + " ";
                        }
                    }
                }
            }        
            */

            // Natural language Discovery query reading passages
            if (discoveryResult.document_passages != null) {
                /* all passages
                if (discoveryResult.document_passages.length > 0) {
                    for (int i = 0; i < discoveryResult.document_passages.length; i++) {
                        DocumentPassage passage = discoveryResult.document_passages[i];
                        String passageText = passage.passage_text;
                        passageText = passageText.replace("<em>", "");
                        passageText = passageText.replace("</em>", "");
                        passageText = passageText.replace("\u0000", "");                        
                        output = output + passageText + " ";
                    }
                }
                */

                // only first passage
                if (discoveryResult.document_passages.length > 0) {                    
                    DocumentPassage passage = discoveryResult.document_passages[0];
                    String passageText = passage.passage_text;
                    passageText = passageText.replace("<em>", "");
                    passageText = passageText.replace("</em>", "");
                    passageText = passageText.replace("\u0000", "");                        
                    output = output + passageText + " ";
                }
            }
        }               
        return output;
    }
}
