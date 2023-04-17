package com.ibm.question_answering.api;

public class PassageAnswer {

    public static String FIELD_SUMMARY = "summary";

    public PassageAnswer(String answerText, double confidence) {
        this.answer_text = answerText;
        this.confidence = confidence;
    }

    public String answer_text;
    public String field;
    public double confidence;
}
