package com.ibm.question_answering.api;

public class DocumentPassage {
    
    public final static String FIELD_TEXT = "text";
    public final static String FIELD_SUMMARY = "summary";

    public DocumentPassage(String passageText, String field, PassageAnswer[] passageAnswers) {
        this.passage_text = passageText;
        this.passageAnswers = passageAnswers;
        this.field = field;
        if (field == null) {
            this.field = FIELD_TEXT;
        }
    }

    public String passage_text;
    public String field;
    public PassageAnswer[] passageAnswers;
}
