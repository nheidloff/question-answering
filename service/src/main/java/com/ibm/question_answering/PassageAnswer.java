package com.ibm.question_answering;

public class PassageAnswer {

    public PassageAnswer(String answerText, double confidence) {
        this.answer_text = answerText;
        this.confidence = confidence;
    }

    public String answer_text;
    public double confidence;
}
